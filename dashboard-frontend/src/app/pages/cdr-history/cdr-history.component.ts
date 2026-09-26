import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CdrService } from '../../services/cdr.service';
import { AuthService } from '../../services/auth.service';
import { CdrRecord, CdrStats } from '../../models/alert.model';

@Component({
  selector: 'app-cdr-history',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatTooltipModule
  ],
  templateUrl: './cdr-history.component.html',
  styleUrls: ['./cdr-history.component.scss']
})
export class CdrHistoryComponent implements OnInit {
  displayedColumns: string[] = [
    'serviceType',
    'callerMsisdn',
    'calleeMsisdn',
    'startTime',
    'durationSeconds',
    'dataVolumeMb',
    'cost',
    'status',
    'actions'
  ];

  dataSource = new MatTableDataSource<CdrRecord>([]);
  totalElements = 0;
  loading = false;

  stats: CdrStats | null = null;

  // Filtres
  filterMsisdn = '';
  filterServiceType = '';
  filterStatus = '';

  // Détail sélectionné
  selectedCdr: CdrRecord | null = null;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private cdrService: CdrService,
    public authService: AuthService
  ) {}

  ngOnInit() {
    this.loadStats();
    this.loadCdrs();
  }

  loadStats() {
    this.cdrService.getCdrStats().subscribe({
      next: (s) => (this.stats = s),
      error: (err) => console.error('Erreur stats CDR:', err)
    });
  }

  loadCdrs() {
    this.loading = true;
    const page = this.paginator ? this.paginator.pageIndex : 0;
    const size = this.paginator ? this.paginator.pageSize : 15;

    this.cdrService
      .getCdrHistory(
        page,
        size,
        this.filterMsisdn || undefined,
        this.filterServiceType || undefined,
        this.filterStatus || undefined
      )
      .subscribe({
        next: (res) => {
          this.dataSource.data = res.content;
          this.totalElements = res.totalElements;
          this.loading = false;
        },
        error: (err) => {
          console.error('Erreur chargement CDR:', err);
          this.loading = false;
        }
      });
  }

  applyFilter() {
    if (this.paginator) {
      this.paginator.pageIndex = 0;
    }
    this.loadCdrs();
  }

  resetFilter() {
    this.filterMsisdn = '';
    this.filterServiceType = '';
    this.filterStatus = '';
    this.applyFilter();
  }

  selectCdr(cdr: CdrRecord) {
    this.selectedCdr = cdr;
  }

  closeDetail() {
    this.selectedCdr = null;
  }

  formatDuration(seconds: number): string {
    if (seconds < 60) return `${seconds}s`;
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}m ${s}s`;
  }
}
