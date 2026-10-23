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

/* FASE 12.0.10 - Conversational AI UX */
(() => {
    const form =
        document.querySelector(
            "[data-chat-composer]");

    const input =
        document.querySelector(
            "[data-chat-input]");

    if (!form || !input) {
        return;
    }

    const submit =
        form.querySelector(
            "[data-chat-submit]");

    const counter =
        form.querySelector(
            "[data-chat-character-count]");

    const confirmationNotice =
        form.querySelector(
            "[data-chat-confirmation-notice]");

    const aiMode =
        form.getAttribute(
            "data-chat-ai-mode") === "true";

    const normalize = value =>
        String(value || "")
            .normalize("NFD")
            .replace(
                /[\u0300-\u036f]/g,
                "")
            .toLowerCase()
            .trim();

    const isExplicitConfirmation = value => {
        if (!aiMode) {
            return false;
        }

        const normalized =
            normalize(value);

        return (
            normalized.includes(
                "confirmar borrador") ||
            normalized.includes(
                "confirmo mi reserva") ||
            normalized.includes(
                "confirmar mi reserva") ||
            normalized.includes(
                "confirmar reserva ahora") ||
            normalized.includes(
                "crear reserva ahora")
        );
    };

    const updateCounter = () => {
        if (!counter) {
            return;
        }

        const max =
            Number.parseInt(
                input.getAttribute(
                    "maxlength") || "0",
                10);

        const current =
            input.value.length;

        counter.textContent =
            max > 0
                ? `${current} / ${max}`
                : String(current);
    };

    const updateConfirmationNotice = () => {
        if (!confirmationNotice) {
            return;
        }

        confirmationNotice.hidden =
            !isExplicitConfirmation(
                input.value);
    };

    const updateUi = () => {
        updateCounter();
        updateConfirmationNotice();
    };

    for (
        const button of
        document.querySelectorAll(
            "[data-chat-suggestion]")
    ) {
        button.addEventListener(
            "click",
            () => {
                const suggestion =
                    button.getAttribute(
                        "data-chat-suggestion");

                if (!suggestion) {
                    return;
                }

                input.value =
                    suggestion;

                input.dispatchEvent(
                    new Event(
                        "input",
                        {
                            bubbles: true
                        }));

                input.focus();
            });
    }

    input.addEventListener(
        "input",
        updateUi);

    form.addEventListener(
        "submit",
        event => {
            if (
                isExplicitConfirmation(
                    input.value)
            ) {
                const accepted =
                    window.confirm(
                        "Este mensaje solicita confirmar o crear una reserva real. "
                        + "El backend volvera a validar disponibilidad, capacidad "
                        + "y reglas de negocio. ¿Deseas enviarlo?");

                if (!accepted) {
                    event.preventDefault();
                    input.focus();
                    return;
                }
            }

            if (submit) {
                submit.disabled = true;
                submit.textContent =
                    "Enviando...";
            }
        });

    updateUi();
})();
