/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.filemanagement.locking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.SystemUtils;
import org.junit.Test;
import org.kitodo.api.filemanagement.LockResult;
import org.kitodo.api.filemanagement.LockingMode;
import org.kitodo.filemanagement.FileManagement;

public class LockManagementTest {
    // Some people
    private static final String ALICE = "Smith, Adelaide";
    private static final String BOB = "Smith, Robert";
    private static final String CAROL = "Smith, Carolin";
    private static final String DAVE = "Smith, David";
    private static final String FRANK = "Smith, Frank";

    private static final String MESSAGE_ALICE_ALLOWED = "Alice should have been allowed to access";
    private static final String MESSAGE_ALICE_LOCK = "Alice should have been able to extend her lock";
    private static final String MESSAGE_BOB_ALLOWED = "Bob should have been allowed to access";
    private static final String MESSAGE_BOB_NOT_ALLOWED = "Bob should not have been allowed to access";
    private static final String MESSAGE_BOB_LOCK = "Bob should not have been able to extend his lock";
    private static final String MESSAGE_PROBLEM = "Bob should have learned that Alice is his problem";
    private static final String MESSAGE_TEMP_FILES = "There should be exactly two temporary files";

    /*
     * Some URIs. These URIs can only be used for tests that do not require to copy
     * the files.
     */
    private static final URI AN_URI = new File("Lorem ipsum").toURI();

    /**
     * The lock management to be tested.
     */
    private final LockManagement underTest = LockManagement.getInstance();

    /**
     * If a user has an exclusive lock on one URI, the other user can not get
     * another lock on that URI. The exception is a fixed read lock, which is not
     * granted unless the file is being written.
     */
    @Test
    public void testExclusiveLockIsExclusive() throws IOException {
        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, AN_URI, LockingMode.EXCLUSIVE), null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

        LockResult noAccessForBob = underTest.tryLock(createRequest(BOB, AN_URI, LockingMode.UPGRADEABLE_READ), null);
        assertTrue(MESSAGE_BOB_NOT_ALLOWED, noAccessForBob instanceof DeniedAccess);

        noAccessForBob = underTest.tryLock(createRequest(BOB, AN_URI, LockingMode.UPGRADE_WRITE_ONCE), null);
        assertTrue(MESSAGE_BOB_NOT_ALLOWED, noAccessForBob instanceof DeniedAccess);

        try (VigilantOutputStream streamGuard = underTest.reportGrant(AN_URI, new NullOutputStream(), alicesAccess)) {
            noAccessForBob = underTest.tryLock(createRequest(BOB, AN_URI, LockingMode.IMMUTABLE_READ), null);
            assertTrue(MESSAGE_BOB_NOT_ALLOWED, noAccessForBob instanceof DeniedAccess);
        }
    }

    /**
     * If a user already has a lock, he can extend it to other files, if possible.
     */
    @Test
    public void testExtendingLocks() throws IOException {
        final URI existingURI1 = File.createTempFile("an_existing_file", ".xml").toURI();
        final URI existingURI2 = File.createTempFile("another_existing_file", ".xml").toURI();

        for (LockingMode firstLock : LockingMode.values()) {
            for (LockingMode lockToAdd : LockingMode.values()) {
                underTest.clear();

                LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, existingURI1, firstLock), null);
                assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

                Map<URI, Collection<String>> noConflictForAlice = alicesAccess
                        .tryLock(createRequest(existingURI2, lockToAdd));
                assertTrue(MESSAGE_ALICE_LOCK, noConflictForAlice.entrySet().isEmpty());
            }
        }
    }

    /**
     * If a user has received an immutable read lock on an URI, another user may
     * thereafter still receive exclusive access to the same URI. After the first
     * user returned the read lock, the temporary file created for it was deleted.
     */
    @Test
    public void testImmutableReadLockingWithLaterCleanUp() throws IOException {
        final URI existingURI1 = File.createTempFile("an_existing_file", ".xml").toURI();

        underTest.clear();

        try (LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, existingURI1, LockingMode.IMMUTABLE_READ),
            null)) {
            assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

            try (LockResult bobsAccess = underTest
                    .tryLock(createRequest(ALICE, existingURI1, LockingMode.IMMUTABLE_READ), null)) {
                assertTrue(MESSAGE_BOB_ALLOWED, bobsAccess instanceof GrantedAccess);
            }

        }
        assertEquals("Temporary file should have been deleted", 0, listTempFiles(existingURI1).length);
    }

    /**
     * Multiple users can get an immutable read lock on a URI, even after another
     * user has been granted an other type of access to the same URI. He also gets
     * access to the before state of the file. Only after the last user who received
     * a fixed read lock has returned the read lock will the temporary file created
     * for it be deleted.
     */
    @Test
    public void testImmutableReadLockingWithMultipleUsers() throws IOException {
        final URI existingURI = File.createTempFile("an_existing_file", ".xml").toURI();

        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, existingURI, LockingMode.IMMUTABLE_READ),
            null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);
        assertEquals("One temporary file would have had to be created", 1, listTempFiles(existingURI).length);
        URI alicesTempFile = ((ImmutableReadLock) ((GrantedAccess) alicesAccess).getLock(existingURI))
                .getImmutableReadCopyURI();

        LockResult bobsAccess = underTest.tryLock(createRequest(BOB, existingURI, LockingMode.IMMUTABLE_READ), null);
        assertTrue(MESSAGE_BOB_ALLOWED, bobsAccess instanceof GrantedAccess);
        assertEquals("There should be exactly one temporary file", 1, listTempFiles(existingURI).length);
        URI bobsTempFile = ((ImmutableReadLock) ((GrantedAccess) bobsAccess).getLock(existingURI))
                .getImmutableReadCopyURI();

        assertEquals("Bob should have been given the same URI to read as Alice", alicesTempFile, bobsTempFile);

        LockResult carolsAccess = underTest.tryLock(createRequest(CAROL, existingURI, LockingMode.EXCLUSIVE), null);
        assertTrue("Carol should have been allowed to access", carolsAccess instanceof GrantedAccess);

        LockResult davesAccess = underTest.tryLock(createRequest(DAVE, existingURI, LockingMode.IMMUTABLE_READ), null);
        assertTrue("Dave should have been allowed to access", davesAccess instanceof GrantedAccess);
        assertEquals("There should be exactly one temporary file", 1, listTempFiles(existingURI).length);
        URI davesTempFile = ((ImmutableReadLock) ((GrantedAccess) davesAccess).getLock(existingURI))
                .getImmutableReadCopyURI();
        assertEquals("Dave should have been given the same URI to read as Alice", alicesTempFile, davesTempFile);

        mimicWriting(underTest, existingURI, (GrantedAccess) carolsAccess);

        LockResult franksAccess = underTest.tryLock(createRequest(FRANK, existingURI, LockingMode.IMMUTABLE_READ),
            null);
        assertTrue("Frank should have been allowed to access", franksAccess instanceof GrantedAccess);
        assertEquals("There should be exactly two temporary files", 2, listTempFiles(existingURI).length);
        URI franksTempFile = ((ImmutableReadLock) ((GrantedAccess) franksAccess).getLock(existingURI))
                .getImmutableReadCopyURI();
        assertNotEquals("Frank should have been given a different URI to read than Alice", alicesTempFile, franksTempFile);

        bobsTempFile = ((ImmutableReadLock) ((GrantedAccess) bobsAccess).getLock(existingURI))
                .getImmutableReadCopyURI();
        assertEquals("Bob should have been given the same URI to read as Alice", alicesTempFile, bobsTempFile);

        LockResult bobsSecondAccess = underTest.tryLock(createRequest(BOB, existingURI, LockingMode.IMMUTABLE_READ),
            null);
        assertTrue(MESSAGE_BOB_ALLOWED, bobsSecondAccess instanceof GrantedAccess);
        assertEquals(MESSAGE_TEMP_FILES, 2, listTempFiles(existingURI).length);
        URI bobsSecondAccessTempFile = ((ImmutableReadLock) ((GrantedAccess) bobsSecondAccess).getLock(existingURI))
                .getImmutableReadCopyURI();
        assertEquals("Bob should again have been given the same URI to read as Alice", alicesTempFile,
            bobsSecondAccessTempFile);

        davesAccess.close();
        assertEquals(MESSAGE_TEMP_FILES, 2, listTempFiles(existingURI).length);
        alicesAccess.close();
        assertEquals(MESSAGE_TEMP_FILES, 2, listTempFiles(existingURI).length);
        bobsAccess.close();
        assertEquals(MESSAGE_TEMP_FILES, 2, listTempFiles(existingURI).length);
        bobsSecondAccess.close();
        assertEquals("There should be exactly one temporary file", 1, listTempFiles(existingURI).length);
        franksAccess.close();
        assertEquals("There shouldn’t be any temporary file", 0, listTempFiles(existingURI).length);
    }

    /**
     * A user can return a lock on a file if it has closed all streams through that
     * lock. If not, he gets an IllegalStateException.
     */
    @Test(expected = IllegalStateException.class)
    public void testLockCanOnlyBeReturnedIfAllStreamsAreClosedForOpenInputStream() throws IOException {
        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, AN_URI, LockingMode.UPGRADEABLE_READ), null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

        try (InputStream openInputStream = underTest.reportGrant(AN_URI, new NullInputStream(0), alicesAccess)) {
            alicesAccess.close();
        }
    }

    /**
     * A user can return a lock on a file if it has closed all streams through that
     * lock. If not, he gets an IllegalStateException.
     */
    @Test(expected = IllegalStateException.class)
    public void testLockCanOnlyBeReturnedIfAllStreamsAreClosedForOpenOutputStream() throws IOException {
        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, AN_URI, LockingMode.EXCLUSIVE), null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

        try (OutputStream openOutputStream = underTest.reportGrant(AN_URI, new NullOutputStream(), alicesAccess)) {
            alicesAccess.close();
        }
    }

    /**
     * If a user requests multiple locks, they will only be granted if all locks are
     * possible, otherwise no locks will be granted.
     */
    @Test
    public void testMultiLocking() throws IOException {
        final URI existingURI1 = File.createTempFile("an_existing_file", ".xml").toURI();
        final URI existingURI2 = File.createTempFile("another_existing_file", ".xml").toURI();

        for (LockingMode firstLock : LockingMode.values()) {
            for (LockingMode secondLock : LockingMode.values()) {
                Map<URI, LockingMode> request = new TreeMap<>();
                request.put(existingURI1, firstLock);
                request.put(existingURI2, secondLock);
                underTest.clear();
                LockResult alicesAccess = underTest.tryLock(new LockRequests(ALICE, request), null);
                assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);
            }
        }

        for (LockingMode firstLock : LockingMode.values()) {
            for (LockingMode secondLock : LockingMode.values()) {
                underTest.clear();

                LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, existingURI1, LockingMode.EXCLUSIVE),
                    null);
                assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

                Map<URI, LockingMode> requestTwoURIs = new TreeMap<>();
                requestTwoURIs.put(existingURI1, firstLock);
                requestTwoURIs.put(existingURI2, secondLock);
                LockResult noAccessForBob;
                try (OutputStream aliceIsWriting = underTest.reportGrant(existingURI1, new NullOutputStream(),
                    alicesAccess)) {
                    noAccessForBob = underTest.tryLock(new LockRequests(BOB, requestTwoURIs), null);
                }
                assertTrue(MESSAGE_BOB_NOT_ALLOWED, noAccessForBob instanceof DeniedAccess);
                assertTrue("Bob should know which URI failed", noAccessForBob.getConflicts().containsKey(existingURI1));
                assertFalse("Bob should know which URI not failed",
                    noAccessForBob.getConflicts().containsKey(existingURI2));
                assertTrue(MESSAGE_PROBLEM,
                    noAccessForBob.getConflicts().get(existingURI1).contains(ALICE));
            }
        }
    }

    /**
     * A realistic scenario: Alice and Bob work on different daily editions of a
     * newspaper. Meanwhile, Carol can change the overall total of the newspaper.
     */
    @Test
    public void testMultiUserNewspaperEditingScenario() throws IOException {
        final URI newspaper = File.createTempFile("newspaper", ".xml").toURI();
        final URI year = File.createTempFile("year", ".xml").toURI();
        final URI anotherYear = File.createTempFile("anotherYear", ".xml").toURI();
        final URI anIssue = File.createTempFile("issue", ".xml").toURI();
        final URI anotherIssue = File.createTempFile("anotherIssue", ".xml").toURI();

        underTest.clear();

        // Alice opens a newspaper issue
        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, anIssue, LockingMode.EXCLUSIVE), null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);
        underTest.checkPermission(alicesAccess, anIssue, false);
        mimicReading(underTest, anIssue, (GrantedAccess) alicesAccess);

        // The meta-data editor finds relations to superior totals and follows
        // them recursively
        Map<URI, Collection<String>> alicesYearAccessConflicts = alicesAccess
                .tryLock(createRequest(year, LockingMode.IMMUTABLE_READ));
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesYearAccessConflicts.entrySet().isEmpty());
        URI alicesYearCopy = underTest.checkPermission(alicesAccess, year, false);
        mimicReading(underTest, alicesYearCopy, (GrantedAccess) alicesAccess);

        Map<URI, Collection<String>> alicesNewspaperAccessConflicts = alicesAccess
                .tryLock(createRequest(newspaper, LockingMode.IMMUTABLE_READ));
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesNewspaperAccessConflicts.entrySet().isEmpty());
        URI alicesNewspaperYearCopy = underTest.checkPermission(alicesAccess, newspaper, false);
        mimicReading(underTest, alicesNewspaperYearCopy, (GrantedAccess) alicesAccess);

        // Bob opens a different newspaper issue
        LockResult bobsAccess = underTest.tryLock(createRequest(BOB, anotherIssue, LockingMode.EXCLUSIVE), null);
        assertTrue(MESSAGE_BOB_ALLOWED, bobsAccess instanceof GrantedAccess);
        underTest.checkPermission(bobsAccess, anotherIssue, false);
        mimicReading(underTest, anotherIssue, (GrantedAccess) bobsAccess);

        // Again, the meta-data editor finds relations to superior totals and
        // follows them recursively
        Map<URI, Collection<String>> bobsYearAccessConflicts = bobsAccess
                .tryLock(createRequest(year, LockingMode.IMMUTABLE_READ));
        assertTrue(MESSAGE_BOB_ALLOWED, bobsYearAccessConflicts.entrySet().isEmpty());
        URI bobsYearCopy = underTest.checkPermission(bobsAccess, year, false);
        mimicReading(underTest, bobsYearCopy, (GrantedAccess) bobsAccess);

        Map<URI, Collection<String>> bobsNewspaperAccessConflicts = bobsAccess
                .tryLock(createRequest(newspaper, LockingMode.IMMUTABLE_READ));
        assertTrue(MESSAGE_BOB_ALLOWED, bobsNewspaperAccessConflicts.entrySet().isEmpty());
        URI bobsNewspaperYearCopy = underTest.checkPermission(bobsAccess, newspaper, false);
        mimicReading(underTest, bobsNewspaperYearCopy, (GrantedAccess) bobsAccess);

        // Alice clicks ‘save’
        underTest.checkPermission(alicesAccess, anIssue, true);
        mimicWriting(underTest, anIssue, (GrantedAccess) alicesAccess);

        // Carol opens the overall edition of the newspaper
        LockResult carolsAccess = underTest.tryLock(createRequest(CAROL, newspaper, LockingMode.EXCLUSIVE), null);
        assertTrue("Carol should have been allowed to access", carolsAccess instanceof GrantedAccess);
        underTest.checkPermission(carolsAccess, newspaper, false);
        mimicReading(underTest, newspaper, (GrantedAccess) carolsAccess);

        // The meta-data editor finds relations to subordinated units and reads
        // them
        Map<URI, LockingMode> carolsYearsRequest = new TreeMap<>();
        carolsYearsRequest.put(year, LockingMode.UPGRADEABLE_READ);
        carolsYearsRequest.put(anotherYear, LockingMode.UPGRADEABLE_READ);
        Map<URI, Collection<String>> carolsYearAccessConflicts = carolsAccess.tryLock(carolsYearsRequest);
        assertTrue("Carol should have been allowed to access", carolsYearAccessConflicts.entrySet().isEmpty());
        underTest.checkPermission(carolsAccess, year, false);
        mimicReading(underTest, year, (GrantedAccess) carolsAccess);
        underTest.checkPermission(carolsAccess, anotherYear, false);
        mimicReading(underTest, anotherYear, (GrantedAccess) carolsAccess);

        // Bob clicks ‘save’
        underTest.checkPermission(bobsAccess, anotherIssue, true);
        mimicWriting(underTest, anotherIssue, (GrantedAccess) carolsAccess);

        // Carol wants to remove the link for the second year from the overall
        // edition of the newspaper
        Map<URI, Collection<String>> carolsLockUpgradeConflicts = carolsAccess
                .tryLock(createRequest(anotherYear, LockingMode.UPGRADE_WRITE_ONCE));
        assertTrue("Carol should have been allowed to upgrade her lock",
            carolsLockUpgradeConflicts.entrySet().isEmpty());
        underTest.checkPermission(carolsAccess, anotherYear, false);
        mimicReading(underTest, anotherYear, (GrantedAccess) carolsAccess);
        underTest.checkPermission(carolsAccess, anotherYear, true);
        mimicWriting(underTest, anotherYear, (GrantedAccess) carolsAccess);
        underTest.checkPermission(carolsAccess, newspaper, true);
        mimicWriting(underTest, newspaper, (GrantedAccess) carolsAccess);

        // Alice clicks ‘save’
        underTest.checkPermission(alicesAccess, anIssue, true);
        mimicWriting(underTest, anIssue, (GrantedAccess) alicesAccess);

        // Bob clicks ‘save’
        underTest.checkPermission(bobsAccess, anotherIssue, true);
        mimicWriting(underTest, anotherIssue, (GrantedAccess) bobsAccess);
    }

    /**
     * Two users cannot simultaneously request an exclusive lock on a URI.
     */
    @Test
    public void testMutualExclusionOfExclusiveLock() {
        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, AN_URI, LockingMode.EXCLUSIVE), null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

        LockResult noAccessForBob = underTest.tryLock(createRequest(BOB, AN_URI, LockingMode.EXCLUSIVE), null);
        assertTrue(MESSAGE_BOB_NOT_ALLOWED, noAccessForBob instanceof DeniedAccess);
        assertTrue("Bob should have been reported Alice as preventer",
            noAccessForBob.getConflicts().get(AN_URI).contains(ALICE));
    }

    /**
     * A user cannot open a read stream to a URI for which he wanted to get
     * permission before, but was rejected.
     */
    @Test(expected = AccessDeniedException.class)
    public void testReadingRequiresGrantedPermissionForExclusiveLock() throws IOException {
        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, AN_URI, LockingMode.EXCLUSIVE), null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

        LockResult noAccessForBob = underTest.tryLock(createRequest(BOB, AN_URI, LockingMode.EXCLUSIVE), null);
        assertTrue(MESSAGE_BOB_NOT_ALLOWED, noAccessForBob instanceof DeniedAccess);

        new FileManagement().read(AN_URI, noAccessForBob);
    }

    /**
     * A user cannot open a read stream to a URI for which he wanted to get
     * permission before, but was rejected.
     */
    @Test(expected = AccessDeniedException.class)
    public void testReadingRequiresGrantedPermissionForImmutableReadLock() throws IOException {
        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, AN_URI, LockingMode.EXCLUSIVE), null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

        LockResult noAccessForBob;
        try (OutputStream aliceIsWriting = underTest.reportGrant(AN_URI, new NullOutputStream(), alicesAccess)) {
            noAccessForBob = underTest.tryLock(createRequest(BOB, AN_URI, LockingMode.IMMUTABLE_READ), null);
            assertTrue(MESSAGE_BOB_NOT_ALLOWED, noAccessForBob instanceof DeniedAccess);
        }

        new FileManagement().write(AN_URI, noAccessForBob);
    }

    /**
     * A user cannot open a read stream to a URI for which he wanted to get
     * permission before, but was rejected.
     */
    @Test(expected = AccessDeniedException.class)
    public void testReadingRequiresGrantedPermissionForUpgradeableReadLock() throws IOException {
        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, AN_URI, LockingMode.EXCLUSIVE), null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

        LockResult noAccessForBob = underTest.tryLock(createRequest(BOB, AN_URI, LockingMode.UPGRADEABLE_READ), null);
        assertTrue(MESSAGE_BOB_NOT_ALLOWED, noAccessForBob instanceof DeniedAccess);

        new FileManagement().read(AN_URI, noAccessForBob);
    }

    /**
     * A user cannot open an input stream to a URI for which he has not previously
     * obtained permission.
     */
    @Test(expected = AccessDeniedException.class)
    public void testReadingRequiresPermission() throws IOException {
        new FileManagement().read(AN_URI, null);
    }

    /**
     * Multiple users can get an upgradeable read lock on a file, but only one user
     * at a time can expand its read lock for a one-time write. As part of the
     * contract, the user who wants to rewrite the file must read it first. While at
     * least one user has an extensible read lock on a URI, no user can get
     * exclusive access to the URI. If he tries, he gets back the names of the lock
     * owners.
     */
    @Test
    public void testUpgradeableReadLocking() throws IOException {
        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, AN_URI, LockingMode.UPGRADEABLE_READ), null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

        LockResult bobsAccess = underTest.tryLock(createRequest(BOB, AN_URI, LockingMode.UPGRADEABLE_READ), null);
        assertTrue(MESSAGE_BOB_ALLOWED, bobsAccess instanceof GrantedAccess);

        Map<URI, Collection<String>> noConflictForAlice = alicesAccess
                .tryLock(createRequest(AN_URI, LockingMode.UPGRADE_WRITE_ONCE));
        assertTrue(MESSAGE_ALICE_LOCK, noConflictForAlice.entrySet().isEmpty());

        Map<URI, Collection<String>> bobsConflict = bobsAccess
                .tryLock(createRequest(AN_URI, LockingMode.UPGRADE_WRITE_ONCE));
        assertFalse(MESSAGE_BOB_LOCK, bobsConflict.entrySet().isEmpty());
        assertTrue(MESSAGE_PROBLEM, bobsConflict.get(AN_URI).contains(ALICE));

        try (InputStream aliceIsReading = underTest.reportGrant(AN_URI, new NullInputStream(0), alicesAccess)) {
            bobsConflict = bobsAccess.tryLock(createRequest(AN_URI, LockingMode.UPGRADE_WRITE_ONCE));
            assertFalse(MESSAGE_BOB_LOCK, bobsConflict.entrySet().isEmpty());
            assertTrue(MESSAGE_PROBLEM, bobsConflict.get(AN_URI).contains(ALICE));
        }

        bobsConflict = bobsAccess.tryLock(createRequest(AN_URI, LockingMode.UPGRADE_WRITE_ONCE));
        assertFalse(MESSAGE_BOB_LOCK, bobsConflict.entrySet().isEmpty());
        assertTrue(MESSAGE_PROBLEM, bobsConflict.get(AN_URI).contains(ALICE));

        try (OutputStream aliceIsWriting = underTest.reportGrant(AN_URI, new NullOutputStream(), alicesAccess)) {
            bobsConflict = bobsAccess.tryLock(createRequest(AN_URI, LockingMode.UPGRADE_WRITE_ONCE));
            assertFalse(MESSAGE_BOB_LOCK, bobsConflict.entrySet().isEmpty());
            assertTrue(MESSAGE_PROBLEM, bobsConflict.get(AN_URI).contains(ALICE));
        }

        Map<URI, Collection<String>> bobsMove = bobsAccess
                .tryLock(createRequest(AN_URI, LockingMode.UPGRADE_WRITE_ONCE));
        assertTrue("Bob should have been able to extend his lock", bobsMove.entrySet().isEmpty());

        Map<URI, Collection<String>> alicesConflict = alicesAccess
                .tryLock(createRequest(AN_URI, LockingMode.UPGRADE_WRITE_ONCE));
        assertFalse("Alice should not have been able to extend her lock", alicesConflict.entrySet().isEmpty());
        assertTrue("Alice should have learned that Bob is her problem", alicesConflict.get(AN_URI).contains(BOB));
    }

    /**
     * Multiple users can get an extensible read lock on a file, but only one user
     * at a time can expand its read lock for a one-time write. The user who wants
     * to rewrite the file must read it first, otherwise he will get a contract
     * infringement exception.
     */
    @Test(expected = ProtocolException.class)
    public void testUpgradeableWriteLockingEnforcesRereading() throws IOException {
        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, AN_URI, LockingMode.UPGRADEABLE_READ), null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

        Map<URI, Collection<String>> noConflictForAlice = alicesAccess
                .tryLock(createRequest(AN_URI, LockingMode.UPGRADE_WRITE_ONCE));
        assertTrue(MESSAGE_ALICE_LOCK, noConflictForAlice.entrySet().isEmpty());

        underTest.checkPermission(alicesAccess, AN_URI, true);
    }

    /**
     * Multiple users can get an extensible read lock on a file, but only one user
     * at a time can expand its read lock for a one-time write. As part of the
     * contract, the user who wants to rewrite the file must read it first. Then he
     * can write her, but only once, then the upgrade expires and he gets an
     * exception.
     */
    @Test(expected = AccessDeniedException.class)
    public void testUpgradeableWriteLockingExpires() throws IOException {
        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, AN_URI, LockingMode.UPGRADEABLE_READ), null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

        Map<URI, Collection<String>> noConflictForAlice = alicesAccess
                .tryLock(createRequest(AN_URI, LockingMode.UPGRADE_WRITE_ONCE));
        assertTrue("Alice should have been able to extend her lock", noConflictForAlice.entrySet().isEmpty());

        mimicReading(underTest, AN_URI, (GrantedAccess) alicesAccess);
        mimicWriting(underTest, AN_URI, (GrantedAccess) alicesAccess);

        underTest.checkPermission(alicesAccess, AN_URI, true);
    }

    /**
     * A user cannot open an output stream to a URI for which he has immutable read
     * permission.
     */
    @Test(expected = AccessDeniedException.class)
    public void testWritingNotAllowedForImmutableReadLock() throws IOException {
        final URI existingURI = File.createTempFile("an_existing_file", ".xml").toURI();

        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, existingURI, LockingMode.IMMUTABLE_READ),
            null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

        underTest.checkPermission(alicesAccess, AN_URI, true);
    }

    /**
     * A user cannot open an output stream to a URI for which he has upgradeable
     * read permission.
     */
    @Test(expected = AccessDeniedException.class)
    public void testWritingNotAllowedForUpgradeableReadLock() throws IOException {
        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, AN_URI, LockingMode.UPGRADEABLE_READ), null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

        underTest.checkPermission(alicesAccess, AN_URI, true);
    }

    /**
     * A user cannot open a read stream to a URI for which he wanted to get
     * permission before, but was rejected.
     */
    @Test(expected = AccessDeniedException.class)
    public void testWritingRequiresGrantedPermissionForExclusiveLock() throws IOException {
        underTest.clear();

        LockResult alicesAccess = underTest.tryLock(createRequest(ALICE, AN_URI, LockingMode.EXCLUSIVE), null);
        assertTrue(MESSAGE_ALICE_ALLOWED, alicesAccess instanceof GrantedAccess);

        LockResult noAccessForBob = underTest.tryLock(createRequest(BOB, AN_URI, LockingMode.EXCLUSIVE), null);
        assertTrue(MESSAGE_BOB_NOT_ALLOWED, noAccessForBob instanceof DeniedAccess);

        underTest.checkPermission(noAccessForBob, AN_URI, true);
    }

    /**
     * A user cannot open an output stream to a URI for which he has not previously
     * obtained permission.
     */
    @Test(expected = AccessDeniedException.class)
    public void testWritingRequiresPermission() throws IOException {
        underTest.clear();
        underTest.checkPermission(null, AN_URI, true);
    }

    // === Supporting functions for the tests ===

    /**
     * Creates a map with the lock request with the two parameters in it.
     *
     * @param uri
     *            URI for which a lock is to be requested
     * @param mode
     *            type of requested lock
     * @return a map with the lock request
     */
    private static Map<URI, LockingMode> createRequest(URI uri, LockingMode mode) {
        TreeMap<URI, LockingMode> result = new TreeMap<>();
        result.put(uri, mode);
        return result;
    }

    /**
     * Creates a map with the lock request with the two parameters in it.
     *
     * @param uri
     *            URI for which a lock is to be requested
     * @param mode
     *            type of requested lock
     * @return a map with the lock request
     */
    private static LockRequests createRequest(String user, URI uri, LockingMode mode) {
        return new LockRequests(user, createRequest(uri, mode));
    }

    /**
     * Counts the number of found temporary files.
     *
     * @return the temporary files
     */
    private static String[] listTempFiles(URI uri) {
        File file = new File(uri.getPath());
        String prefix = file.getName();
        if (SystemUtils.IS_OS_WINDOWS) {
            prefix = prefix.replace('.', '_');
        }
        final String finalPrefix = prefix.concat("-");
        List<String> result = Arrays.asList(file.getParentFile().list((dir, name) -> name.startsWith(finalPrefix)));
        return result.toArray(new String[result.size()]);
    }

    /**
     * Mimic the lock management that the file would be read. The lock management
     * generates a stream guard for the transferred stream. When the stream guard is
     * closed, the lock management believes the file was read.
     *
     * @param lockManagement
     *            lock management to fool
     * @param uri
     *            URI of stream
     * @param access
     *            granted access
     */
    private static void mimicReading(LockManagement lockManagement, URI uri, GrantedAccess access) throws IOException {
        lockManagement.reportGrant(uri, new NullInputStream(0), access).close();
    }

    /**
     * Mimic the lock management that the file would be written. The lock management
     * generates a stream guard for the transferred stream. When the stream guard is
     * closed, the lock management believes the file was written.
     *
     * @param lockManagement
     *            lock management to fool
     * @param uri
     *            URI of stream
     * @param access
     *            granted access
     */
    private static void mimicWriting(LockManagement lockManagement, URI uri, GrantedAccess access) throws IOException {
        lockManagement.reportGrant(uri, new NullOutputStream(), access).close();
    }
}
