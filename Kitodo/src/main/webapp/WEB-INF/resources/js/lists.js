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

function registerRowToggleEvents(event) {
    // add listener to expanded rows
    $(".ui-expanded-row").hover(function() {
            $(this).next(".ui-expanded-row-content").addClass("row-hover");
        }, function () {
            $(this).next(".ui-expanded-row-content").removeClass("row-hover");
        }
    );
    $(".ui-expanded-row:hover").next(".ui-expanded-row-content").addClass("row-hover");

    // add listener to expanded rows content
    $(".ui-expanded-row-content").hover(function() {
            $(this).prev(".ui-expanded-row:last").addClass("row-hover");
        }, function () {
            $(this).prev(".ui-expanded-row:last").removeClass("row-hover");
        }
    );
}

$( document ).on( "click", ".allSelectable .ui-chkbox-all .ui-chkbox-box", function() {
    if( $(this).hasClass( "ui-state-active" ) ) {
        PF('allSelectableOverlayPanel').show();
    } else {
        PF('processesTable').unselectAllRows();
    }
});