"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const navigation =
        document.getElementById("bePublicNavigation");

    if (!navigation) {
        return;
    }

    const links =
        navigation.querySelectorAll("a");

    links.forEach((link) => {
        link.addEventListener("click", () => {
            if (!window.bootstrap) {
                return;
            }

            const collapse =
                window.bootstrap.Collapse.getInstance(
                    navigation);

            if (collapse) {
                collapse.hide();
            }
        });
    });
});
