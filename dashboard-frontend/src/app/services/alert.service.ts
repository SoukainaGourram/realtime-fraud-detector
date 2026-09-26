import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Alert, Stats } from '../models/alert.model';

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  private apiUrl = '/api/v1/alerts';

  constructor(private http: HttpClient) {}

  getAlerts(page: number, size: number, msisdn?: string, riskLevel?: string, status?: string): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (msisdn) params = params.set('callerMsisdn', msisdn);
    if (riskLevel) params = params.set('riskLevel', riskLevel);
    if (status) params = params.set('status', status);

    return this.http.get<any>(this.apiUrl, { params });
  }

  getAlertById(id: number): Observable<Alert> {
    return this.http.get<Alert>(`${this.apiUrl}/${id}`);
  }

  getStats(): Observable<Stats> {
    return this.http.get<Stats>(`${this.apiUrl}/stats`);
  }

  reviewAlert(id: number, status: string): Observable<Alert> {
    const params = new HttpParams().set('newStatus', status);
    return this.http.put<Alert>(`${this.apiUrl}/${id}/review`, {}, { params });
  }
}
