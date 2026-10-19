const getApiBaseUrl = (): string => {
  if (typeof window !== 'undefined') {
    const custom = localStorage.getItem('API_BASE_URL');
    if (custom && custom.trim()) {
      return custom.trim().replace(/\/+$/, '');
    }
  }
  return 'https://finance-tracker-backend.onrender.com';
};

export const environment = {
  production: true,
  apiBaseUrl: getApiBaseUrl()
};
