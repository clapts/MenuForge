async function loadMenu() {
  const response = await fetch('/api/menu');
  if (!response.ok) {
    throw new Error(`Menu request failed: ${response.status}`);
  }

  const menu = await response.json();
  const root = document.querySelector('[data-menu-root]');
  root.innerHTML = '';

  for (const category of menu.categories) {
    const section = document.createElement('section');
    section.dataset.category = category.slug;
    section.innerHTML = `<h2>${escapeHtml(category.title)}</h2>`;

    for (const item of category.items || []) {
      const article = document.createElement('article');
      article.dataset.item = item.id;
      article.innerHTML = `
        <div>
          <h3>${escapeHtml(item.title)}</h3>
          <p>${escapeHtml(item.description || '')}</p>
          <small>${(item.ingredients || []).map(escapeHtml).join(', ')}</small>
        </div>
        <strong>${escapeHtml(item.price || '')}</strong>
      `;
      section.appendChild(article);
    }

    root.appendChild(section);
  }
}

function escapeHtml(value) {
  return String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
}

document.addEventListener('DOMContentLoaded', () => {
  loadMenu().catch((error) => {
    console.error(error);
    document.querySelector('[data-menu-root]').textContent = 'Menu non disponibile.';
  });
});
