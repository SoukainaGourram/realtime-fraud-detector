import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { interval, Subscription } from 'rxjs';
import { AlertService } from '../../services/alert.service';
import { CdrService } from '../../services/cdr.service';
import { WebsocketService } from '../../services/websocket.service';
import { AuthService } from '../../services/auth.service';
import { Alert, Stats, CdrStats } from '../../models/alert.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatTooltipModule,
    BaseChartDirective
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  stats: Stats | null = null;
  cdrStats: CdrStats | null = null;
  liveAlerts: Alert[] = [];
  
  chartData: ChartData<'bar'> = {
    labels: ['FAIBLE', 'MOYEN', 'ÉLEVÉ', 'CRITIQUE'],
    datasets: [
      {
        data: [0, 0, 0, 0],
        label: 'Alertes par Niveau de Risque',
        backgroundColor: [
          'rgba(0, 230, 118, 0.75)',
          'rgba(255, 202, 40, 0.75)',
          'rgba(255, 152, 0, 0.75)',
          'rgba(255, 82, 82, 0.85)'
        ],
        borderColor: ['#00e676', '#ffca28', '#ff9800', '#ff5252'],
        borderWidth: 1,
        borderRadius: 6
      }
    ]
  };
  
  chartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false
      }
    },
    scales: {
      x: {
        grid: { color: 'rgba(255, 255, 255, 0.05)' },
        ticks: { color: '#8c93b0' }
      },
      y: {
        beginAtZero: true,
        grid: { color: 'rgba(255, 255, 255, 0.05)' },
        ticks: { color: '#8c93b0', stepSize: 1 }
      }
    }
  };

  private subs: Subscription = new Subscription();

  constructor(
    private alertService: AlertService,
    private cdrService: CdrService,
    public wsService: WebsocketService,
    public authService: AuthService
  ) {}

  ngOnInit() {
    this.loadStats();
    this.loadCdrStats();

    // Auto-refresh toutes les 30s
    this.subs.add(
      interval(30000).subscribe(() => {
        this.loadStats();
        this.loadCdrStats();
      })
    );

    this.wsService.connect();
    this.subs.add(
      this.wsService.alerts$.subscribe(alert => {
        this.liveAlerts.unshift(alert);
        if (this.liveAlerts.length > 12) {
          this.liveAlerts.pop();
        }
      })
    );
  }

  loadStats() {
    this.alertService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.chartData = {
          labels: ['FAIBLE', 'MOYEN', 'ÉLEVÉ', 'CRITIQUE'],
          datasets: [
            {
              data: [
                data.countByLevel.LOW,
                data.countByLevel.MEDIUM,
                data.countByLevel.HIGH,
                data.countByLevel.CRITICAL
              ],
              label: 'Alertes par Niveau',
              backgroundColor: [
                'rgba(0, 230, 118, 0.75)',
                'rgba(255, 202, 40, 0.75)',
                'rgba(255, 152, 0, 0.75)',
                'rgba(255, 82, 82, 0.85)'
              ],
              borderColor: ['#00e676', '#ffca28', '#ff9800', '#ff5252'],
              borderWidth: 1,
              borderRadius: 6
            }
          ]
        };
      },
      error: err => console.error(err)
    });
  }

  loadCdrStats() {
    this.cdrService.getCdrStats().subscribe({
      next: (s) => (this.cdrStats = s),
      error: (err) => console.error('Erreur stats CDR:', err)
    });
  }

  ngOnDestroy() {
    this.subs.unsubscribe();
  }
}
