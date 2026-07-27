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
rer-dsp-core (instalação / config / Compose)
    ↓
rer-dsp-job-data-migration (ETL)
rer-dsp-job-geo-file-generation (geoespacial)
```

A configuração de instalação (hierarquia, telas, KPIs) **não** fica neste repositório.
Fonte: `rer-dsp-core/config/installation/installation-config.json` (env `DSP_INSTALLATION_CONFIG_FILE`).

## 🚀 Setup

Preferível subir pela stack do core (`rer-dsp-core/./start.sh`).

```bash
# Dev local (layout irmão DSP/rer-dsp-core + DSP/rer-dsp-backend)
./gradlew bootRun
# usa por padrão: file:../rer-dsp-core/config/installation/installation-config.json
```

## 📖 Documentação

- [RER — Visão Geral](https://github.com/Rural-Environmental-Registry)
- [SDD (System Design Document)](https://github.com/Rural-Environmental-Registry/core)

## 📜 Licença

Este projeto é licenciado sob a [GNU General Public License v3.0](LICENSE).
