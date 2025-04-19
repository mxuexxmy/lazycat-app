const fetchApps = async () => {
  try {
    const url = `http://localhost:8080/api/apps/category/${route.params.id}`
    const response = await fetch(url)
    const result = await response.json()
    if (Array.isArray(result)) {
      apps.value = result
    }
  } catch (error) {
    console.error('Failed to fetch apps:', error)
  }
} 