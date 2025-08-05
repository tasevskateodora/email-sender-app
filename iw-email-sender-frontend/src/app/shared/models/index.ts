// Email Job Model
export interface EmailJob {
  id?: string;
  senderEmail: string;
  receiverEmails: string;
  startDate: string;
  endDate?: string;
  sendTime: string;
  recurrencePattern: RecurrencePattern;
  enabled: boolean;
  oneTime: boolean;
  nextRunTime?: string;
  emailTemplate?: EmailTemplate;
  createdBy?: User;
  createdAt?: string;
  updatedAt?: string;
}

export enum RecurrencePattern {
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  MONTHLY = 'MONTHLY',
  YEARLY = 'YEARLY',
  ONE_TIME='ONE_TIME'

}

export interface EmailJobResponse {
  success: boolean;
  message: string;
  jobId: string;
  enabled: boolean;
  timestamp: string;
}

// Email Template Model
export interface EmailTemplate {
  id?: string;
  name: string;
  subject: string;
  body: string;
  createdAt?: string;
  updatedAt?: string;
}

// User Model
export interface User {
  id?: string;
  username: string;
  email?: string;
  password?: string;
  enabled?: boolean;
  roles?: Role[];
  createdAt?: string;
  updatedAt?: string;
}

// Role Model
export interface Role {
  id?: string;
  name: string;
  createdAt?: string;
  updatedAt?: string;
}

// Login interfaces
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  id: string;
  username: string;
  email: string;
  roles: string[];
}
