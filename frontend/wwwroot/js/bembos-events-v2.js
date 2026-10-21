(() => {
    'use strict';

    const normalize = (value) => (value || '')
        .replace(/\s+/g, ' ')
        .trim();

    const textOf = (el) => normalize(el ? el.textContent : '');

    const escapeHtml = (value) => String(value || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');

    function findLinkByText(root, expression) {
        const links = [...root.querySelectorAll('a[href]')];
        return links.find(a => expression.test(textOf(a))) || null;
    }

    function findHeroCandidate() {
        const preferred = [...document.querySelectorAll(
            '[class*="hero"], [class*="banner"], section, main > div'
        )];

        return preferred.find(el => {
            const t = textOf(el).toUpperCase();
            return t.includes('FIESTA') &&
                   (t.includes('COLOR') || t.includes('CELEBR'));
        }) || null;
    }

    function enhanceHomeHero() {
        const hero = findHeroCandidate();
        if (!hero || hero.dataset.beV2 === '1') return;

        const sedesLink = findLinkByText(hero, /sede|reserv|evento|comienza/i);
        const paquetesLink = findLinkByText(document, /paquete/i);
        const sedesHref = sedesLink?.getAttribute('href') || '/sedes';
        const paquetesHref = paquetesLink?.getAttribute('href') || '/paquetes';

        hero.dataset.beV2 = '1';
        hero.className = 'be-home-hero';
        hero.innerHTML = `
            <span class="be-confetti be-confetti--a"></span>
            <span class="be-confetti be-confetti--b"></span>
            <span class="be-confetti be-confetti--c"></span>

            <div class="be-hero-inner">
                <div class="be-hero-copy">
                    <span class="be-hero-kicker">Bembos Events</span>
                    <h1 class="be-hero-title">
                        Tu fiesta.<br>
                        <em>Tu momento.</em>
                    </h1>
                    <p class="be-hero-text">
                        Convierte tu celebraciÃ³n en una experiencia inolvidable con espacios,
                        temÃ¡ticas y paquetes preparados para disfrutar en grande.
                    </p>
                    <div class="be-hero-actions">
                        <a class="be-hero-button be-hero-button--primary" href="${escapeHtml(sedesHref)}">
                            Comienza tu evento
                        </a>
                        <a class="be-hero-button be-hero-button--secondary" href="${escapeHtml(paquetesHref)}">
                            Ver paquetes
                        </a>
                    </div>
                </div>

                <aside class="be-hero-floating" aria-hidden="true">
                    <div class="be-hero-floating-icon">â˜…</div>
                    <strong>Hazlo inolvidable</strong>
                    <span>Celebraciones que se sienten diferentes.</span>
                </aside>
            </div>`;
    }

    function findVenueRoot() {
        const headings = [...document.querySelectorAll('h1, h2')];
        const title = headings.find(h => /plaza norte|dev\s*-\s*/i.test(textOf(h)));
        if (!title) return null;

        const main = title.closest('main');
        if (main) return { root: main, title };

        const container = title.closest('.container, .container-fluid');
        if (container) return { root: container, title };

        return { root: title.parentElement || document.body, title };
    }

    function captureLineAfterLabel(raw, labelRegex) {
        const lines = (raw || '')
            .split(/\r?\n/)
            .map(x => x.trim())
            .filter(Boolean);

        const index = lines.findIndex(x => labelRegex.test(x));
        if (index >= 0 && index + 1 < lines.length) {
            return lines[index + 1];
        }
        return '';
    }

    function enhanceVenuePage() {
        const hit = findVenueRoot();
        if (!hit) return;

        const { root, title } = hit;
        if (root.dataset.beVenueV2 === '1') return;

        const raw = root.innerText || '';
        const originalTitle = textOf(title);
        const cleanTitle = originalTitle
            .replace(/^DEV\s*-\s*/i, '')
            .replace(/^Bembos\s+/i, '')
            .trim();

        const venueTitle = cleanTitle
            ? `Bembos ${cleanTitle}`
            : 'Bembos Events';

        const address = captureLineAfterLabel(raw, /^Direcci[oÃ³]n$/i) || 'Av. Alfredo Mendiola 1400';
        const district = captureLineAfterLabel(raw, /^Distrito$/i) || 'Independencia';
        const phone = captureLineAfterLabel(raw, /^Tel[eÃ©]fono$/i) || '999 111 111';

        const capacityMatch = raw.match(/Capacidad\s*:\s*([^\n\r]+)/i);
        const capacity = capacityMatch ? normalize(capacityMatch[1]) : '40 personas';

        const salonHeading = [...root.querySelectorAll('h2, h3, h4')]
            .map(x => textOf(x))
            .find(x => /sal[oÃ³]n/i.test(x) && !/^salones/i.test(x));

        const salonName = salonHeading
            ? salonHeading.replace(/^DEV\s*-\s*/i, '')
            : 'SalÃ³n Norte';

        const backLink = findLinkByText(root, /volver.*sede/i);
        const salonLink = findLinkByText(root, /ver.*sal[oÃ³]n/i);
        const backHref = backLink?.getAttribute('href') || '/sedes';
        const salonHref = salonLink?.getAttribute('href') || '#';

        const descriptiveParagraph = [...root.querySelectorAll('p')]
            .map(p => textOf(p))
            .find(t => /sal[oÃ³]n|reserva|celebr|fiesta/i.test(t) && t.length > 25);

        const salonDescription = descriptiveParagraph ||
            'Un espacio preparado para celebrar, compartir y disfrutar una experiencia Bembos con familia y amigos.';

        document.body.classList.add('be-venue-active');
        root.dataset.beVenueV2 = '1';
        root.className = 'be-venue-page';

        root.innerHTML = `
            <div class="be-venue-shell">
                <a href="${escapeHtml(backHref)}" class="be-venue-back">â† Volver a sedes</a>

                <section class="be-venue-hero">
                    <div class="be-venue-photo">
                        <span class="be-venue-location">â— ${escapeHtml(district)}</span>
                    </div>

                    <div class="be-venue-content">
                        <span class="be-venue-kicker">Tu prÃ³xima celebraciÃ³n</span>
                        <h1 class="be-venue-title">${escapeHtml(venueTitle)}</h1>
                        <p class="be-venue-description">
                            Encuentra el espacio ideal para celebrar tu evento y vivir una experiencia
                            Bembos cÃ³moda, divertida y lista para compartir.
                        </p>

                        <div class="be-venue-meta">
                            <div class="be-meta-item">
                                <div class="be-meta-icon">âŒ–</div>
                                <div class="be-meta-copy">
                                    <small>DirecciÃ³n</small>
                                    <strong>${escapeHtml(address)}</strong>
                                </div>
                            </div>

                            <div class="be-meta-item">
                                <div class="be-meta-icon">â—†</div>
                                <div class="be-meta-copy">
                                    <small>Distrito</small>
                                    <strong>${escapeHtml(district)}</strong>
                                </div>
                            </div>

                            <div class="be-meta-item">
                                <div class="be-meta-icon">â˜Ž</div>
                                <div class="be-meta-copy">
                                    <small>TelÃ©fono</small>
                                    <strong>${escapeHtml(phone)}</strong>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <section class="be-salons-section">
                    <header class="be-section-head">
                        <div>
                            <span class="be-section-kicker">Nuestros espacios</span>
                            <h2>Elige tu salÃ³n</h2>
                        </div>
                        <p>
                            Revisa la capacidad y conoce el espacio disponible para tu celebraciÃ³n.
                        </p>
                    </header>

                    <article class="be-salon-card">
                        <div class="be-salon-photo">
                            <span class="be-salon-capacity">â— Hasta ${escapeHtml(capacity)}</span>
                        </div>

                        <div class="be-salon-body">
                            <span class="be-salon-kicker">${escapeHtml(cleanTitle || district)}</span>
                            <h3>${escapeHtml(salonName)}</h3>
                            <p>${escapeHtml(salonDescription)}</p>

                            <div class="be-salon-footer">
                                <span class="be-salon-note">Ideal para cumpleaÃ±os y celebraciones</span>
                                <a href="${escapeHtml(salonHref)}" class="be-salon-button">Ver salÃ³n â†’</a>
                            </div>
                        </div>
                    </article>
                </section>
            </div>`;
    }

    function boot() {
        try { enhanceHomeHero(); } catch (error) { console.error('[Bembos V2] Hero:', error); }
        try { enhanceVenuePage(); } catch (error) { console.error('[Bembos V2] Venue:', error); }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', boot, { once: true });
    } else {
        boot();
    }
})();