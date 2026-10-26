(() => {
    const search =
        document.getElementById(
            "r4-location-search");

    const filter =
        document.getElementById(
            "r4-location-filter");

    const count =
        document.getElementById(
            "r4-location-count");

    const empty =
        document.getElementById(
            "r4-location-empty-filter");

    const cards =
        Array.from(
            document.querySelectorAll(
                "[data-r4-location]"));

    if (!search || !filter || cards.length === 0) {
        return;
    }

    const normalize = value =>
        String(value ?? "")
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .toLowerCase()
            .trim();

    const apply = () => {
        const query =
            normalize(search.value);

        const mode =
            filter.value;

        let visible = 0;

        for (const card of cards) {
            const haystack =
                normalize(
                    card.dataset.search);

            const isEvent =
                card.dataset.events ===
                "true";

            const matchesQuery =
                !query ||
                haystack.includes(query);

            const matchesMode =
                mode === "all" ||
                (
                    mode === "events" &&
                    isEvent
                ) ||
                (
                    mode === "info" &&
                    !isEvent
                );

            const show =
                matchesQuery &&
                matchesMode;

            card.hidden =
                !show;

            if (show) {
                visible += 1;
            }
        }

        if (count) {
            count.textContent =
                String(visible);
        }

        if (empty) {
            empty.hidden =
                visible !== 0;
        }
    };

    search.addEventListener(
        "input",
        apply);

    filter.addEventListener(
        "change",
        apply);

    apply();
})();