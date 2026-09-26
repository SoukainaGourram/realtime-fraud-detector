import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AlertService } from '../../services/alert.service';
import { AuthService } from '../../services/auth.service';
import { Alert, AlertStatus, RiskLevel } from '../../models/alert.model';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatProgressBarModule,
    MatTooltipModule
  ],
  templateUrl: './alerts.component.html',
  styleUrls: ['./alerts.component.scss']
})
export class AlertsComponent implements OnInit {
  displayedColumns: string[] = ['callerMsisdn', 'alertType', 'riskScore', 'riskLevel', 'status', 'createdAt', 'actions'];
  dataSource = new MatTableDataSource<Alert>([]);
  totalElements = 0;
  loading = false;

  filterMsisdn = '';
  filterRiskLevel = '';
  filterStatus = '';

  selectedAlert: Alert | null = null;
  parsedRules: any = null;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private alertService: AlertService,
    private http: HttpClient,
    public authService: AuthService
  ) {}

  ngOnInit() {
    this.loadAlerts();
  }

  loadAlerts() {
    this.loading = true;
    const page = this.paginator ? this.paginator.pageIndex : 0;
    const size = this.paginator ? this.paginator.pageSize : 10;

    this.alertService.getAlerts(
      page,
      size,
      this.filterMsisdn || undefined,
      this.filterRiskLevel as RiskLevel || undefined,
      this.filterStatus as AlertStatus || undefined
    ).subscribe({
      next: (data) => {
        this.dataSource.data = data.content;
        this.totalElements = data.totalElements;
        if (this.sort) {
          this.dataSource.sort = this.sort;
        }
        this.loading = false;
      },
      error: err => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  applyFilter() {
    if (this.paginator) {
      this.paginator.pageIndex = 0;
    }
    this.loadAlerts();
  }

  resetFilter() {
    this.filterMsisdn = '';
    this.filterRiskLevel = '';
    this.filterStatus = '';
    this.applyFilter();
  }

  openDetail(alert: Alert) {
    this.selectedAlert = alert;
    try {
      this.parsedRules = JSON.parse(alert.triggeredRules);
    } catch {
      this.parsedRules = alert.triggeredRules;
    }
  }

  closeDetail() {
    this.selectedAlert = null;
  }

  reviewAlert(id: number, status: string) {
    this.alertService.reviewAlert(id, status).subscribe({
      next: (updatedAlert) => {
        if (this.selectedAlert && this.selectedAlert.id === id) {
          this.selectedAlert.status = updatedAlert.status;
        }
        this.loadAlerts();
      },
      error: err => console.error(err)
    });
  }

  exportCsv() {
    this.http.get('/api/v1/alerts/export/csv', { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `telecom_fraud_alerts_${new Date().toISOString().slice(0,10)}.csv`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Erreur export CSV:', err)
    });
  }
}
