const baseUrl = 'https://ristorante.example.com/api/menu/admin';
const apiKey = process.env.MENUFORGE_ADMIN_KEY;

async function hideItem(itemId) {
  const response = await fetch(`${baseUrl}/items/${itemId}/toggle-availability`, {
    method: 'PATCH',
    headers: {
      'X-MenuForge-Key': apiKey
    }
  });

  if (!response.ok) {
    throw new Error(`MenuForge admin request failed: ${response.status}`);
  }

  return response.json();
}

async function importFullMenu(menuDocument) {
  const response = await fetch(`${baseUrl}/import`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'X-MenuForge-Key': apiKey
    },
    body: JSON.stringify(menuDocument)
  });

  if (!response.ok) {
    throw new Error(`MenuForge import failed: ${response.status}`);
  }

  return response.json();
}

export { hideItem, importFullMenu };
