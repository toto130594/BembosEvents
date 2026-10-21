(() => {
  'use strict';

  // R6 SAFETY GUARD - DO NOT MUTATE R6 PAGES
  if (
    document.querySelector('.be-r6-header') ||
    document.querySelector('.be-r6-hero') ||
    document.querySelector('link[href*="bembos-events-r6.css"]')
  ) {
    return;
  }
const txt = el => (el && el.textContent ? el.textContent : '').replace(/\s+/g,' ').trim();
  const esc = s => String(s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#039;');
  const findLink = (root,re) => [...(root || document).querySelectorAll('a[href]')].find(a => re.test(txt(a))) || null;
  const pageText = () => txt(document.body).toUpperCase();

  function homeLooksRelevant(){
    const t = pageText();
    return (t.includes('FIESTA') && (t.includes('COLOR') || t.includes('CELEBR') || t.includes('TRANSFORM'))) ||
           (t.includes('BEMBOS') && t.includes('PAQUETES') && t.includes('TEMATICAS'));
  }

  function locateHero(){
    const nodes = [...document.querySelectorAll('main section, main > div, section, [class*="hero"], [class*="banner"]')];
    const scored = nodes.map(el => {
      const t = txt(el).toUpperCase();
      let score = 0;
      if(t.includes('FIESTA')) score += 4;
      if(t.includes('COLOR')) score += 4;
      if(t.includes('TRANSFORM')) score += 4;
      if(t.includes('CELEBR')) score += 3;
      if(t.length > 30) score += 1;
      if(t.length > 600) score -= 3;
      return {el,score};
    }).sort((a,b)=>b.score-a.score);
    return scored.length && scored[0].score >= 4 ? scored[0].el : null;
  }

  function enhanceHome(){
    if(document.getElementById('be-r3-home-hero')) return;
    if(!homeLooksRelevant()) return;

    let hero = locateHero();
    const sedes = findLink(document,/sede|reserv|evento|comienza/i);
    const packs = findLink(document,/paquete/i);
    const sedesHref = sedes ? sedes.getAttribute('href') : '/sedes';
    const packsHref = packs ? packs.getAttribute('href') : '/paquetes';

    const markup = `
      <div class="be-r3-bg"></div><div class="be-r3-glow"></div>
      <div class="be-r3-inner">
        <div class="be-r3-copy">
          <span class="be-r3-kicker">BEMBOS EVENTS</span>
          <h1>Tu fiesta.<br><em>Tu momento.</em></h1>
          <p>Celebra en grande con espacios, temÃ¡ticas y paquetes preparados para convertir cada momento en una experiencia inolvidable.</p>
          <div class="be-r3-actions">
            <a class="be-r3-btn be-r3-btn-primary" href="${esc(sedesHref)}">Comienza tu evento</a>
            <a class="be-r3-btn be-r3-btn-secondary" href="${esc(packsHref)}">Ver paquetes</a>
          </div>
        </div>
        <aside class="be-r3-float"><div class="be-r3-star">â˜…</div><b>Hazlo inolvidable</b><span>Fiestas que se sienten diferentes.</span></aside>
      </div>`;

    if(hero){
      hero.id = 'be-r3-home-hero';
      hero.className = '';
      hero.innerHTML = markup;
    } else {
      const main = document.querySelector('main') || document.body;
      const block = document.createElement('section');
      block.id = 'be-r3-home-hero';
      block.innerHTML = markup;
      main.insertBefore(block, main.firstChild);
    }
    document.documentElement.dataset.beR3Home = 'applied';
  }

  function getLines(root){
    return (root.innerText || '').split(/\r?\n/).map(x=>x.trim()).filter(Boolean);
  }
  function afterLabel(lines,re){
    const i = lines.findIndex(x=>re.test(x));
    return i >= 0 && i + 1 < lines.length ? lines[i+1] : '';
  }
  function venueLooksRelevant(){
    const t = pageText();
    return (t.includes('VOLVER A SEDES') || t.includes('INFORMACION DE LA SEDE') || t.includes('INFORMACIÃ“N DE LA SEDE')) &&
           (t.includes('SALONES') || t.includes('SALON'));
  }
  function locateVenueRoot(){
    const back = findLink(document,/volver.*sede/i);
    if(back){
      return back.closest('main') || back.closest('.container') || back.parentElement;
    }
    const h = [...document.querySelectorAll('h1,h2')].find(x=>/plaza norte|dev\s*-|sede/i.test(txt(x)));
    return h ? (h.closest('main') || h.closest('.container') || h.parentElement) : null;
  }

  function enhanceVenue(){
    if(document.getElementById('be-r3-venue')) return;
    if(!venueLooksRelevant()) return;
    const root = locateVenueRoot();
    if(!root) return;

    const lines = getLines(root);
    const raw = root.innerText || '';
    const titleEl = [...root.querySelectorAll('h1,h2')].find(x=>!/salones/i.test(txt(x))) || root.querySelector('h1,h2');
    const original = titleEl ? txt(titleEl) : 'Bembos Events';
    const clean = original.replace(/^DEV\s*-\s*/i,'').replace(/^Bembos\s+/i,'').trim();
    const venueTitle = /^Bembos\s/i.test(original) ? original : `Bembos ${clean || 'Events'}`;
    const address = afterLabel(lines,/^Direcci[oÃ³]n$/i) || 'Av. Alfredo Mendiola 1400';
    const district = afterLabel(lines,/^Distrito$/i) || 'Independencia';
    const phone = afterLabel(lines,/^Tel[eÃ©]fono$/i) || '999 111 111';
    const cap = (raw.match(/Capacidad\s*:\s*([^\n\r]+)/i) || [,'40 personas'])[1].trim();
    const back = findLink(root,/volver.*sede/i);
    const salon = findLink(root,/ver.*sal[oÃ³]n/i);
    const salonHeading = [...root.querySelectorAll('h2,h3,h4')].map(txt).find(x=>/sal[oÃ³]n/i.test(x) && !/^salones/i.test(x));
    const salonName = salonHeading ? salonHeading.replace(/^DEV\s*-\s*/i,'') : 'SalÃ³n Norte';
    const p = [...root.querySelectorAll('p')].map(txt).find(x=>x.length>25 && /sal[oÃ³]n|reserva|celebr|fiesta/i.test(x));
    const desc = p || 'Un espacio preparado para celebrar y disfrutar una experiencia Bembos con familia y amigos.';

    root.id = 'be-r3-venue';
    root.className = '';
    document.body.classList.add('be-r3-venue-body');
    root.innerHTML = `
      <div class="be-r3-shell">
        <a class="be-r3-back" href="${esc(back ? back.getAttribute('href') : '/sedes')}">â† Volver a sedes</a>
        <section class="be-r3-venue-hero">
          <div class="be-r3-venue-photo"><span class="be-r3-location">â— ${esc(district)}</span></div>
          <div class="be-r3-venue-content">
            <span class="be-r3-kicker2">Tu prÃ³xima celebraciÃ³n</span>
            <h1>${esc(venueTitle)}</h1>
            <p class="be-r3-desc">Encuentra el espacio ideal para celebrar tu evento con una experiencia Bembos cÃ³moda, divertida y lista para compartir.</p>
            <div class="be-r3-meta">
              <div class="be-r3-meta-item"><div class="be-r3-meta-icon">âŒ–</div><div class="be-r3-meta-copy"><small>DirecciÃ³n</small><strong>${esc(address)}</strong></div></div>
              <div class="be-r3-meta-item"><div class="be-r3-meta-icon">â—†</div><div class="be-r3-meta-copy"><small>Distrito</small><strong>${esc(district)}</strong></div></div>
              <div class="be-r3-meta-item"><div class="be-r3-meta-icon">â˜Ž</div><div class="be-r3-meta-copy"><small>TelÃ©fono</small><strong>${esc(phone)}</strong></div></div>
            </div>
          </div>
        </section>
        <section class="be-r3-salons">
          <div class="be-r3-section-head"><div><span class="be-r3-kicker2">Nuestros espacios</span><h2>Elige tu salÃ³n</h2></div><p>Revisa la capacidad y conoce el espacio disponible para tu celebraciÃ³n.</p></div>
          <article class="be-r3-salon-card">
            <div class="be-r3-salon-photo"><span class="be-r3-capacity">â— Hasta ${esc(cap)}</span></div>
            <div class="be-r3-salon-body"><span class="be-r3-kicker2">${esc(clean || district)}</span><h3>${esc(salonName)}</h3><p>${esc(desc)}</p><div class="be-r3-salon-footer"><span class="be-r3-salon-note">Ideal para cumpleaÃ±os y celebraciones</span><a class="be-r3-salon-btn" href="${esc(salon ? salon.getAttribute('href') : '#')}">Ver salÃ³n â†’</a></div></div>
          </article>
        </section>
      </div>`;
    document.documentElement.dataset.beR3Venue = 'applied';
  }

  function boot(){
    try { enhanceHome(); } catch(e) { console.error('[BEMBOS-R3][HOME]',e); }
    try { enhanceVenue(); } catch(e) { console.error('[BEMBOS-R3][VENUE]',e); }
    console.info('[BEMBOS-R3] cargado', {
      home: document.documentElement.dataset.beR3Home || 'not-applied',
      venue: document.documentElement.dataset.beR3Venue || 'not-applied'
    });
  }
  if(document.readyState === 'loading') document.addEventListener('DOMContentLoaded',boot,{once:true}); else boot();
})();
