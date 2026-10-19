const getApiBaseUrl = (): string => {
  if (typeof window !== 'undefined') {
    const custom = localStorage.getItem('API_BASE_URL');
    if (custom && custom.trim()) {
      const clean = custom.trim().replace(/\/+$/, '');
      if (clean === 'https://finance-tracker-backend.onrender.com' || clean === 'http://finance-tracker-backend.onrender.com') {
        localStorage.removeItem('API_BASE_URL');
      } else {
        return clean;
      }
    }
  }
  return 'https://finance-tracker-backend-nr64.onrender.com';
};

export const environment = {
  production: true,
  apiBaseUrl: getApiBaseUrl()
};
