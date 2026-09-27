# Call Guard — Détection de Fraude & d'Anomalies Télécom en Temps Réel 

Plateforme de détection de fraudes télécom en temps réel. Call Guard analyse des CDR voix et data, génère des alertes et les affiche sur un tableau de bord.

## Fonctionnalités

- Ingestion des CDR avec Apache Kafka
- Détection d’anomalies par règles configurables
- Alertes en temps réel via WebSocket
- Historique et recherche de CDR
- Authentification JWT
- Simulateur de trafic et de scénarios de fraude

## Technologies

Java 21 · Spring Boot · Apache Kafka · PostgreSQL · Angular · Docker Compose

## Démarrage rapide

Prérequis : Docker Desktop et Git.

```bash
git clone https://github.com/SoukainaGourram/realtime-fraud-detector.git
cd realtime-fraud-detector
cp .env.example .env
docker compose up --build -d
