export type AlertType = 'SIMBOX' | 'HIGH_VOLUME' | 'HIGH_DIVERSITY' | 'SHORT_CALL_RATIO' | 'CONTINUOUS_ACTIVITY' | 'CLONED_SIM' | 'HIGH_DATA_VOLUME';
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type AlertStatus = 'NEW' | 'REVIEWED' | 'FALSE_POSITIVE' | 'CONFIRMED';
export type ServiceType = 'VOICE' | 'DATA';

export interface Alert {
  id: number;
  alertId: string;
  callerMsisdn: string;
  alertType: AlertType;
  riskScore: number;
  riskLevel: RiskLevel;
  triggeredRules: string;
  description: string;
  status: AlertStatus;
  createdAt: string;
  reviewedAt?: string;
  reviewedBy?: string;
}

export interface Stats {
  countByLevel: { LOW: number; MEDIUM: number; HIGH: number; CRITICAL: number };
  topAlerts: Alert[];
}

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
  expiresIn: number;
}

export interface CdrRecord {
  id: number;
  callId: string;
  callerMsisdn: string;
  calleeMsisdn: string;
  startTime: string;
  durationSeconds: number;
  callType: string;
  serviceType: ServiceType;
  dataVolumeMb: number;
  apn?: string;
  cellId?: string;
  cost: number;
  status: string;
  createdAt: string;
}

export interface CdrStats {
  totalCdrs: number;
  voiceCount: number;
  dataCount: number;
  totalDataVolumeMb: number;
  totalVoiceDurationMinutes: number;
}
