export interface User {
  id: number;
  name: string;
  email: string;
  phoneNumber?: string;
}

export interface LoginRequest {
  email: string; // Accepts 10-digit phone number or email address
  password: string;
  phoneNumber?: string;
}

export interface RegisterRequest {
  name: string;
  phoneNumber: string;
  password: string;
  otp: string;
  email?: string;
}

export interface SendOtpRequest {
  phoneNumber: string;
  purpose?: string;
}

export interface SendOtpResponse {
  success: boolean;
  message: string;
  phoneNumber: string;
  simulatedOtp?: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  user: User;
  token?: string;
}
