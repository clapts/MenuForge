const state = {
  menu: null,
  selectedCategorySlug: null,
  selectedItemId: null,
  connected: false
};

const els = {
  publicBase: document.querySelector("#publicBase"),
  adminBase: document.querySelector("#adminBase"),
  apiKey: document.querySelector("#apiKey"),
  status: document.querySelector("#status"),
  categoryList: document.querySelector("#categoryList"),
  itemList: document.querySelector("#itemList"),
  customerPreview: document.querySelector("#customerPreview"),
  previewCount: document.querySelector("#previewCount"),
  activeCategoryTitle: document.querySelector("#activeCategoryTitle"),
  activeCategoryMeta: document.querySelector("#activeCategoryMeta"),
  itemForm: document.querySelector("#itemForm"),
  categoryForm: document.querySelector("#categoryForm"),
  badgeForm: document.querySelector("#badgeForm"),
  badgeList: document.querySelector("#badgeList"),
  jsonEditor: document.querySelector("#jsonEditor"),
  searchInput: document.querySelector("#searchInput"),
  availabilityFilter: document.querySelector("#availabilityFilter")
};

const emptyState = () => document.querySelector("#emptyStateTemplate").content.cloneNode(true);

function publicBase() {
  return trimSlash(els.publicBase.value);
}

function adminBase() {
  return trimSlash(els.adminBase.value);
}

function trimSlash(value) {
  return String(value || "").replace(/\/+$/, "");
}

function adminHeaders() {
  return {
    "Content-Type": "application/json",
    "X-MenuForge-Key": els.apiKey.value
  };
}

async function request(url, options = {}) {
  const response = await fetch(url, options);
  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json") ? await response.json() : await response.text();

  if (!response.ok) {
    const message = typeof body === "string" ? body : body.message || JSON.stringify(body);
    throw new Error(`${response.status} ${response.statusText}: ${message}`);
  }

  return body;
}

async function connect() {
  try {
    setStatus("Connessione in corso...");
    state.menu = await request(publicBase());
    state.connected = true;
    if (!state.selectedCategorySlug) {
      state.selectedCategorySlug = state.menu.categories?.[0]?.slug || null;
    }
    state.selectedItemId = currentCategory()?.items?.[0]?.id || null;
    syncJson();
    render();
    setStatus("Connesso", "ok");
  } catch (error) {
    setStatus(error.message, "error");
  }
}

async function refreshFromAdminExport() {
  state.menu = await request(`${adminBase()}/export`, { headers: adminHeaders() });
  if (!currentCategory()) {
    state.selectedCategorySlug = state.menu.categories?.[0]?.slug || null;
  }
  if (!currentItem()) {
    state.selectedItemId = currentCategory()?.items?.[0]?.id || null;
  }
  syncJson();
  render();
}

function setStatus(message, type = "") {
  els.status.textContent = message;
  els.status.className = `status ${type}`.trim();
}

function currentCategory() {
  return state.menu?.categories?.find(category => category.slug === state.selectedCategorySlug) || null;
}

function currentItem() {
  const category = currentCategory();
  return category?.items?.find(item => item.id === state.selectedItemId) || null;
}

function render() {
  renderCategories();
  renderItems();
  renderCustomerPreview();
  renderForms();
  renderBadges();
}

function renderCategories() {
  els.categoryList.innerHTML = "";
  const categories = state.menu?.categories || [];

  if (!categories.length) {
    els.categoryList.append(emptyState());
    return;
  }

  categories
    .slice()
    .sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
    .forEach(category => {
      const row = document.createElement("button");
      row.type = "button";
      row.className = `category-row ${category.slug === state.selectedCategorySlug ? "active" : ""}`;
      row.innerHTML = `
        <div class="row-title">
          <strong>${escapeHtml(category.title || category.slug)}</strong>
          <span>#${category.position ?? 0}</span>
        </div>
        <div class="chips">
          <span class="chip ${category.visible ? "accent" : "warning"}">${category.visible ? "Visibile" : "Nascosta"}</span>
          <span class="chip">${category.items?.length || 0} prodotti</span>
        </div>
      `;
      row.addEventListener("click", () => {
        state.selectedCategorySlug = category.slug;
        state.selectedItemId = category.items?.[0]?.id || null;
        render();
      });
      els.categoryList.append(row);
    });
}

function renderItems() {
  const category = currentCategory();
  els.itemList.innerHTML = "";
  els.activeCategoryTitle.textContent = category?.title || "Prodotti";
  els.activeCategoryMeta.textContent = category
    ? `${category.slug} - ${category.visible ? "categoria visibile" : "categoria nascosta"}`
    : "Seleziona una categoria.";

  if (!category) {
    els.itemList.append(emptyState());
    return;
  }

  const search = els.searchInput.value.trim().toLowerCase();
  const filter = els.availabilityFilter.value;
  const items = (category.items || [])
    .filter(item => itemMatchesSearch(item, search))
    .filter(item => itemMatchesFilter(item, filter))
    .sort((a, b) => (a.position ?? 0) - (b.position ?? 0));

  if (!items.length) {
    els.itemList.append(emptyState());
    return;
  }

  items.forEach(item => {
    const row = document.createElement("button");
    row.type = "button";
    row.className = `item-row ${item.id === state.selectedItemId ? "active" : ""}`;
    row.innerHTML = `
      <div class="row-title">
        <strong>${escapeHtml(item.title || item.id)}</strong>
        <span>${escapeHtml(item.price || "")}</span>
      </div>
      <p class="muted">${escapeHtml(item.description || "Nessuna descrizione")}</p>
      <div class="chips">
        <span class="chip ${item.available ? "accent" : "warning"}">${item.available ? "Disponibile" : "Non disponibile"}</span>
        ${item.highlight ? '<span class="chip accent">In evidenza</span>' : ""}
        ${chipsFor([...(item.tag1 || []), ...(item.tag2 || []), ...(item.tag3 || [])])}
      </div>
    `;
    row.addEventListener("click", () => {
      state.selectedItemId = item.id;
      renderForms();
    });
    els.itemList.append(row);
  });
}

function renderCustomerPreview() {
  const category = currentCategory();
  const availableItems = (category?.items || []).filter(item => item.available);
  els.customerPreview.innerHTML = "";
  els.previewCount.textContent = `${availableItems.length} prodotti`;

  if (!availableItems.length) {
    els.customerPreview.append(emptyState());
    return;
  }

  availableItems.slice(0, 5).forEach(item => {
    const node = document.createElement("article");
    node.className = "preview-item";
    node.innerHTML = `
      <div class="row-title">
        <strong>${escapeHtml(item.title || "")}</strong>
        <span class="price">${escapeHtml(item.price || "")}</span>
      </div>
      <p class="muted">${escapeHtml(item.description || "")}</p>
      <div class="chips">
        ${item.specialText1 ? `<span class="chip accent">${escapeHtml(item.specialText1)}</span>` : ""}
        ${chipsFor((item.allergens || []).map(allergen => allergen.nameIt || allergen.code || allergen.id))}
      </div>
    `;
    els.customerPreview.append(node);
  });
}

function renderForms() {
  fillCategoryForm(currentCategory());
  fillItemForm(currentItem());
  syncJson();
}

function renderBadges() {
  const badges = state.menu?.badges || [];
  els.badgeList.innerHTML = "";

  if (!badges.length) {
    els.badgeList.append(emptyState());
    return;
  }

  badges.forEach(badge => {
    const row = document.createElement("div");
    row.className = "badge-row";
    row.innerHTML = `
      <div class="row-title">
        <strong>${escapeHtml(badge.label || badge.id)}</strong>
        <button class="button danger" type="button" data-delete-badge="${escapeHtml(badge.id)}">Elimina</button>
      </div>
      <div class="chips">
        <span class="chip">${escapeHtml(badge.id || "")}</span>
        <span class="chip accent">${escapeHtml(badge.style || "default")}</span>
      </div>
    `;
    els.badgeList.append(row);
  });
}

function fillCategoryForm(category) {
  const form = els.categoryForm;
  form.slug.value = category?.slug || "";
  form.title.value = category?.title || "";
  form.subtitle.value = category?.subtitle || "";
  form.note.value = category?.note || "";
  form.categoryImageUrl.value = category?.categoryImageUrl || "";
  form.position.value = category?.position ?? "";
  form.visible.checked = category?.visible ?? true;
}

function fillItemForm(item) {
  const form = els.itemForm;
  form.id.value = item?.id || "";
  form.title.value = item?.title || "";
  form.price.value = item?.price || "";
  form.description.value = item?.description || "";
  form.imageUrl.value = item?.imageUrl || "";
  form.position.value = item?.position ?? "";
  form.calories.value = item?.calories || "";
  form.origin.value = item?.origin || "";
  form.format.value = item?.format || "";
  form.specialText1.value = item?.specialText1 || "";
  form.specialText2.value = item?.specialText2 || "";
  form.specialText3.value = item?.specialText3 || "";
  form.ingredients.value = joinList(item?.ingredients);
  form.tag1.value = joinList(item?.tag1);
  form.tag2.value = joinList(item?.tag2);
  form.tag3.value = joinList(item?.tag3);
  form.allergenNumbers.value = joinList((item?.allergens || []).map(allergen => allergen.id));
  form.badgeIds.value = joinList((item?.badges || []).map(badge => badge.id));
  form.available.checked = item?.available ?? true;
  form.highlight.checked = item?.highlight ?? false;
}

function syncJson() {
  els.jsonEditor.value = state.menu ? JSON.stringify(state.menu, null, 2) : "";
}

async function saveCategory(event) {
  event.preventDefault();
  const payload = formPayload(els.categoryForm, {
    position: numberOrNull,
    visible: checkboxValue
  });

  try {
    if (state.selectedCategorySlug) {
      await request(`${adminBase()}/categories/${encodeURIComponent(state.selectedCategorySlug)}`, {
        method: "PUT",
        headers: adminHeaders(),
        body: JSON.stringify(payload)
      });
    } else {
      await request(`${adminBase()}/categories`, {
        method: "POST",
        headers: adminHeaders(),
        body: JSON.stringify(payload)
      });
    }
    state.selectedCategorySlug = payload.slug || state.selectedCategorySlug;
    await refreshFromAdminExport();
    setStatus("Categoria salvata", "ok");
  } catch (error) {
    setStatus(error.message, "error");
  }
}

async function saveItem(event) {
  event.preventDefault();
  const category = currentCategory();
  if (!category) {
    setStatus("Crea o seleziona una categoria prima del prodotto.", "error");
    return;
  }

  const payload = formPayload(els.itemForm, {
    position: numberOrNull,
    available: checkboxValue,
    highlight: checkboxValue,
    ingredients: splitList,
    tag1: splitList,
    tag2: splitList,
    tag3: splitList,
    allergenNumbers: splitNumbers,
    badgeIds: splitList
  });

  try {
    const selected = currentItem();
    if (selected) {
      await request(`${adminBase()}/items/${encodeURIComponent(selected.id)}`, {
        method: "PUT",
        headers: adminHeaders(),
        body: JSON.stringify(payload)
      });
    } else {
      await request(`${adminBase()}/categories/${encodeURIComponent(category.slug)}/items`, {
        method: "POST",
        headers: adminHeaders(),
        body: JSON.stringify(payload)
      });
    }
    state.selectedItemId = payload.id || state.selectedItemId;
    await refreshFromAdminExport();
    setStatus("Prodotto salvato", "ok");
  } catch (error) {
    setStatus(error.message, "error");
  }
}

async function saveBadge(event) {
  event.preventDefault();
  const payload = formPayload(els.badgeForm);

  try {
    const existing = (state.menu?.badges || []).some(badge => badge.id === payload.id);
    await request(`${adminBase()}/badges${existing ? `/${encodeURIComponent(payload.id)}` : ""}`, {
      method: existing ? "PUT" : "POST",
      headers: adminHeaders(),
      body: JSON.stringify(payload)
    });
    els.badgeForm.reset();
    await refreshFromAdminExport();
    setStatus("Badge salvato", "ok");
  } catch (error) {
    setStatus(error.message, "error");
  }
}

async function deleteSelectedCategory() {
  if (!state.selectedCategorySlug) return;
  if (!confirm("Eliminare questa categoria e tutti i suoi prodotti?")) return;

  try {
    await request(`${adminBase()}/categories/${encodeURIComponent(state.selectedCategorySlug)}`, {
      method: "DELETE",
      headers: adminHeaders()
    });
    state.selectedCategorySlug = null;
    state.selectedItemId = null;
    await refreshFromAdminExport();
    setStatus("Categoria eliminata", "ok");
  } catch (error) {
    setStatus(error.message, "error");
  }
}

async function deleteSelectedItem() {
  const item = currentItem();
  if (!item) return;
  if (!confirm("Eliminare questo prodotto?")) return;

  try {
    await request(`${adminBase()}/items/${encodeURIComponent(item.id)}`, {
      method: "DELETE",
      headers: adminHeaders()
    });
    state.selectedItemId = null;
    await refreshFromAdminExport();
    setStatus("Prodotto eliminato", "ok");
  } catch (error) {
    setStatus(error.message, "error");
  }
}

async function deleteBadge(id) {
  try {
    await request(`${adminBase()}/badges/${encodeURIComponent(id)}`, {
      method: "DELETE",
      headers: adminHeaders()
    });
    await refreshFromAdminExport();
    setStatus("Badge eliminato", "ok");
  } catch (error) {
    setStatus(error.message, "error");
  }
}

async function replaceJsonFromEditor() {
  try {
    const documentBody = JSON.parse(els.jsonEditor.value);
    state.menu = await request(`${adminBase()}/import`, {
      method: "PUT",
      headers: adminHeaders(),
      body: JSON.stringify(documentBody)
    });
    state.selectedCategorySlug = state.menu.categories?.[0]?.slug || null;
    state.selectedItemId = currentCategory()?.items?.[0]?.id || null;
    render();
    setStatus("JSON importato", "ok");
  } catch (error) {
    setStatus(error.message, "error");
  }
}

function exportJsonFile() {
  if (!state.menu) return;
  const blob = new Blob([JSON.stringify(state.menu, null, 2)], { type: "application/json" });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = "menuforge-menu.json";
  link.click();
  URL.revokeObjectURL(link.href);
}

async function importJsonFile(event) {
  const file = event.target.files?.[0];
  if (!file) return;

  try {
    const text = await file.text();
    els.jsonEditor.value = text;
    await replaceJsonFromEditor();
  } finally {
    event.target.value = "";
  }
}

function newCategory() {
  state.selectedCategorySlug = null;
  state.selectedItemId = null;
  fillCategoryForm(null);
  activatePanel("categoryPanel");
}

function newItem() {
  state.selectedItemId = null;
  fillItemForm(null);
  activatePanel("itemPanel");
}

function activatePanel(panelId) {
  document.querySelectorAll(".tab").forEach(tab => {
    tab.classList.toggle("active", tab.dataset.panel === panelId);
  });
  document.querySelectorAll(".panel").forEach(panel => {
    panel.classList.toggle("active", panel.dataset.panelId === panelId);
  });
}

function formPayload(form, transforms = {}) {
  const payload = {};
  Array.from(form.elements).forEach(field => {
    if (!field.name) return;
    const transform = transforms[field.name];
    if (transform) {
      payload[field.name] = transform(field);
      return;
    }
    payload[field.name] = field.value.trim();
  });
  return payload;
}

function checkboxValue(field) {
  return field.checked;
}

function numberOrNull(field) {
  return field.value === "" ? null : Number(field.value);
}

function splitList(field) {
  return field.value.split(",").map(value => value.trim()).filter(Boolean);
}

function splitNumbers(field) {
  return splitList(field).map(Number).filter(Number.isFinite);
}

function joinList(values) {
  return (values || []).filter(value => value !== null && value !== undefined && value !== "").join(", ");
}

function itemMatchesSearch(item, search) {
  if (!search) return true;
  const text = [
    item.title,
    item.description,
    item.price,
    ...(item.ingredients || []),
    ...(item.tag1 || []),
    ...(item.tag2 || []),
    ...(item.tag3 || [])
  ].join(" ").toLowerCase();
  return text.includes(search);
}

function itemMatchesFilter(item, filter) {
  if (filter === "available") return item.available;
  if (filter === "unavailable") return !item.available;
  if (filter === "highlight") return item.highlight;
  return true;
}

function chipsFor(values) {
  return (values || [])
    .filter(Boolean)
    .slice(0, 6)
    .map(value => `<span class="chip">${escapeHtml(String(value))}</span>`)
    .join("");
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

document.querySelector("#connectBtn").addEventListener("click", connect);
document.querySelector("#exportBtn").addEventListener("click", exportJsonFile);
document.querySelector("#importFile").addEventListener("change", importJsonFile);
document.querySelector("#newCategoryBtn").addEventListener("click", newCategory);
document.querySelector("#newItemBtn").addEventListener("click", newItem);
document.querySelector("#deleteCategoryBtn").addEventListener("click", deleteSelectedCategory);
document.querySelector("#deleteItemBtn").addEventListener("click", deleteSelectedItem);
document.querySelector("#replaceJsonBtn").addEventListener("click", replaceJsonFromEditor);
document.querySelector("#copyJsonBtn").addEventListener("click", async () => {
  await navigator.clipboard.writeText(els.jsonEditor.value);
  setStatus("JSON copiato", "ok");
});
els.itemForm.addEventListener("submit", saveItem);
els.categoryForm.addEventListener("submit", saveCategory);
els.badgeForm.addEventListener("submit", saveBadge);
els.searchInput.addEventListener("input", renderItems);
els.availabilityFilter.addEventListener("change", renderItems);
els.badgeList.addEventListener("click", event => {
  const button = event.target.closest("[data-delete-badge]");
  if (button) deleteBadge(button.dataset.deleteBadge);
});
document.querySelectorAll(".tab").forEach(tab => {
  tab.addEventListener("click", () => activatePanel(tab.dataset.panel));
});

setStatus("Pronta. Inserisci API e premi Connetti.");
render();

