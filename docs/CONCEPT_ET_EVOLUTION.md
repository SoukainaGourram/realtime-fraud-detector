# 📌 Différence Conceptuelle & Évolution du Projet (Projet Telecom)

---

## 1. La Différence Conceptuelle

### Définition
> **CDR (Call Detail Record)** = l'enregistrement brut que génère le réseau à chaque appel (voix) ou session (data) : *qui, quoi, quand, combien de temps, combien de données*. C'est la donnée source, commune aux deux sujets.

### Tableau Comparatif

| Dimension | Consultation de l'historique des appels (1ᵉʳ sujet) | Monitoring des CDR data + voix (2ᵉ sujet / Call Guard) |
|---|---|---|
| **Ce que ça fait** | Rechercher et consulter des CDR déjà stockés, à la demande | Analyser en continu le flux de CDR au moment où ils arrivent |
| **Portée des données** | Voix uniquement | Voix et data (sessions internet) |
| **Mode de traitement** | Synchrone, à la demande (l'utilisateur cherche) | Asynchrone, en flux continu (le système surveille tout seul) |
| **Objectif métier** | Auditabilité, traçabilité | Détection d'anomalies/fraude en temps réel |
| **Complexité technique** | CRUD + recherche + sécurité | Streaming (Kafka), scoring, alerting temps réel (WebSocket) |

---

## 2. En clair : L'analogie explicative

- **La consultation d'historique**, c'est comme **chercher un dossier dans une armoire** quand on en a besoin.
- **Le monitoring de CDR**, c'est comme **avoir une caméra de surveillance** qui regarde tout en permanence et qui sonne l'alarme toute seule si elle voit quelque chose de louche.

---

## 3. Pourquoi cette distinction compte pour toi (Rapport & Soutenance)

- **Ton premier sujet (consultation)** est le **socle initial** — c'est ce que tu as développé au début du stage, avec *Auth*, *User*, *Call History*, et *Search History Service*.
- **Ton nouveau sujet (Call Guard, monitoring temps réel)** est une **évolution beaucoup plus ambitieuse** de ce socle :
  - Tu conserves une partie de l'architecture éprouvée (les microservices, la sécurité JWT/RBAC).
  - Tu ajoutes une couche d'ingénierie temps réel : le traitement de flux continu **Apache Kafka** et un moteur algorithmique de détection d'anomalies à fenêtrage glissant.
  - Tu élargis la portée aux données **data** en plus de la **voix** pour une couverture globale.

---

## 4. Précision Stratégique (Périmètre du Projet)

> 💡 **Voix vs Data :**  
> Si "data et voix" est une évolution que tu souhaites concrétiser dans le code de Call Guard (analyser également les sessions de données/internet en plus des flux vocaux), cela constitue une extension du modèle de CDR et des scénarios de simulation.  
> C'est également un argument de valorisation majeur à intégrer dans ton rapport de Projet et sur ton CV pour démontrer la vision complète du monitoring télécom.
