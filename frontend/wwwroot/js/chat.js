(() => {
    const root =
        document.querySelector(
            "[data-chat-history-url]");

    if (!root) {
        return;
    }

    const list =
        root.querySelector(
            "[data-chat-messages]");

    const url =
        root.getAttribute(
            "data-chat-history-url");

    if (!list || !url) {
        return;
    }

    let stopped = false;

    const formatDate = value => {
        const date =
            new Date(value);

        if (
            Number.isNaN(
                date.getTime())
        ) {
            return "";
        }

        return date.toLocaleString(
            "es-PE",
            {
                dateStyle: "short",
                timeStyle: "short"
            });
    };

    const senderClass = value => {
        switch (
            String(value || "")
                .toUpperCase()
        ) {
            case "CLIENTE":
                return "cliente";
            case "IA":
                return "ia";
            case "ENCARGADO_LOCAL":
                return "encargado-local";
            case "SISTEMA":
                return "sistema";
            default:
                return "sistema";
        }
    };

    const render = messages => {
        const fragment =
            document.createDocumentFragment();

        for (const message of messages) {
            const article =
                document.createElement(
                    "article");

            article.className =
                "chat-message chat-message--" +
                senderClass(
                    message.tipoEmisor);

            const meta =
                document.createElement(
                    "div");

            meta.className =
                "chat-message__meta";

            const sender =
                document.createElement(
                    "strong");

            sender.textContent =
                String(
                    message.tipoEmisor || "")
                    .replaceAll(
                        "_",
                        " ");

            const time =
                document.createElement(
                    "time");

            time.textContent =
                formatDate(
                    message.fechaEnvio);

            meta.append(
                sender,
                time);

            const body =
                document.createElement(
                    "p");

            body.textContent =
                message.contenido || "";

            article.append(
                meta,
                body);

            fragment.append(
                article);
        }

        list.replaceChildren(
            fragment);

        list.scrollTop =
            list.scrollHeight;
    };

    const refresh = async () => {
        if (stopped) {
            return;
        }

        try {
            const response =
                await fetch(
                    url,
                    {
                        method: "GET",
                        credentials: "same-origin",
                        cache: "no-store",
                        headers: {
                            "X-Requested-With":
                                "XMLHttpRequest"
                        }
                    });

            if (
                response.status === 401 ||
                response.status === 403 ||
                response.status === 404
            ) {
                stopped = true;
                return;
            }

            if (!response.ok) {
                return;
            }

            const messages =
                await response.json();

            if (
                Array.isArray(
                    messages)
            ) {
                render(
                    messages);
            }
        }
        catch {
            // UI remains usable if one polling cycle fails.
        }
    };

    window.setInterval(
        refresh,
        3000);
})();