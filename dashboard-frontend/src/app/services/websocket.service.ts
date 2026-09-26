import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { Alert } from '../models/alert.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private client: Client;
  public alerts$ = new Subject<Alert>();
  public isConnected = false;

  constructor(private authService: AuthService) {
    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.client.onConnect = () => {
      this.isConnected = true;
      this.client.subscribe('/topic/alerts', message => {
        if (message.body) {
          const alert: Alert = JSON.parse(message.body);
          this.alerts$.next(alert);
        }
      });
    };

    this.client.onDisconnect = () => {
      this.isConnected = false;
    };
  }

  connect() {
    if (this.authService.isLoggedIn) {
      this.client.activate();
    }
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
    }
  }
}
