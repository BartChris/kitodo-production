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

package org.kitodo.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * UserGroup DTO object.
 */
public class UserGroupDTO extends BaseDTO {

    private String title;
    private List<UserDTO> users = new ArrayList<>();
    private Integer usersSize;
    private List<AuthorityDTO> authorities = new ArrayList<>();
    private int authorizationsSize;
    private ClientDTO client;

    /**
     * Get title.
     * 
     * @return title as String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     * 
     * @param title
     *            as String
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get list of users.
     *
     * @return list of users as UserDTO
     */
    public List<UserDTO> getUsers() {
        return users;
    }

    /**
     * Set list of users.
     *
     * @param users
     *            list of users as UserDTO
     */
    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }

    /**
     * Get size of users.
     * 
     * @return size of users as Integer
     */
    public Integer getUsersSize() {
        return usersSize;
    }

    /**
     * Set size of users.
     * 
     * @param usersSize
     *            as Integer
     */
    public void setUsersSize(Integer usersSize) {
        this.usersSize = usersSize;
    }

    /**
     * Get list of authorities.
     *
     * @return list of authorities as AuthorityDTO
     */
    public List<AuthorityDTO> getAuthorities() {
        return authorities;
    }

    /**
     * Set list of authorities.
     *
     * @param authorities
     *            list of authorities as AuthorityDTO
     */
    public void setAuthorities(List<AuthorityDTO> authorities) {
        this.authorities = authorities;
    }

    /**
     * Get size of authorities.
     *
     * @return size of authorities as int
     */
    public int getAuthorizationsSize() {
        return authorizationsSize;
    }

    /**
     * Set size of authorities.
     *
     * @param authorizationsSize
     *            as int
     */
    public void setAuthorizationsSize(int authorizationsSize) {
        this.authorizationsSize = authorizationsSize;
    }

    /**
     * Get client FTO object.
     *
     * @return the client DTO object
     */
    public ClientDTO getClient() {
        return client;
    }

    /**
     * Set client DTO object.
     *
     * @param client as DTO object.
     */
    public void setClient(ClientDTO client) {
        this.client = client;
    }
}
