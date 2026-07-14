# RER DSP — Backend

**Projeto**: Rural Environmental Registry — Data Sharing Platform  
**Componente**: Backend (API REST)  
**Tipo**: Digital Public Good (DPG)  
**Licença**: GPL-3.0

---

## 📋 Visão Geral

Serviço backend da plataforma DSP do RER. Expõe APIs REST para acesso e compartilhamento de dados ambientais rurais entre instituições parceiras.

## 🏗️ Arquitetura

Este componente faz parte do ecossistema RER DSP:

```
rer-dsp-frontend (UI)
    ↓
rer-dsp-backend  ← ESTE REPO
    ↓
rer-dsp-core (lógica de domínio)
    ↓
rer-dsp-job-data-migration (ETL)
rer-dsp-job-geo-file-generation (geoespacial)
```

## 🚀 Setup

```bash
# Clonar
git clone https://github.com/Rural-Environmental-Registry/rer-dsp-backend.git
cd rer-dsp-backend

# Instruções de build serão adicionadas conforme desenvolvimento
```

## 📖 Documentação

- [RER — Visão Geral](https://github.com/Rural-Environmental-Registry)
- [SDD (System Design Document)](https://github.com/Rural-Environmental-Registry/core)

## 📜 Licença

Este projeto é licenciado sob a [GNU General Public License v3.0](LICENSE).
