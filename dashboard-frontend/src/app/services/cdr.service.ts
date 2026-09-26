import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CdrRecord, CdrStats } from '../models/alert.model';

@Injectable({
  providedIn: 'root'
})
export class CdrService {
  private apiUrl = '/api/v1/cdrs/history';

  constructor(private http: HttpClient) {}

  getCdrHistory(
    page: number = 0,
    size: number = 15,
    msisdn?: string,
    serviceType?: string,
    status?: string,
    startDate?: string,
    endDate?: string
  ): Observable<{ content: CdrRecord[]; totalElements: number; totalPages: number }> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (msisdn && msisdn.trim()) params = params.set('msisdn', msisdn.trim());
    if (serviceType && serviceType.trim()) params = params.set('serviceType', serviceType.trim());
    if (status && status.trim()) params = params.set('status', status.trim());
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);

    return this.http.get<{ content: CdrRecord[]; totalElements: number; totalPages: number }>(this.apiUrl, { params });
  }

  getCdrStats(): Observable<CdrStats> {
    return this.http.get<CdrStats>(`${this.apiUrl}/stats`);
  }

  getCdrById(id: number): Observable<CdrRecord> {
    return this.http.get<CdrRecord>(`${this.apiUrl}/${id}`);
  }
}
