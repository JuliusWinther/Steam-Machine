# Steam-Machine

Steam-Machine è un'applicazione software sviluppata in Java 22 e JavaFX, progettata per Windows. Questo software emula le principali funzionalità di Steam, consentendo di consultare informazioni su determinati software, scaricarli, disinstallarli e molto altro, con un'interfaccia user-friendly e varie funzionalità aggiuntive. È compatibile con qualsiasi eseguibile di Windows e può avviare direttamente ROM su RetroArch.

## Caratteristiche principali

* Gestione Completa dei Software: Consente di consultare dettagli sui software, scaricarli e disinstallarli comodamente.
* Compatibilità: Supporta qualsiasi eseguibile di Windows e può avviare ROM su RetroArch.
* Integrazione con Steam: Recupera dettagli dei software direttamente da Steam, inclusi descrizione, immagini, video e banner.
* Database Esterno: Utilizza una pagina Google Sheets come database per dettagli su software personalizzati non presenti su Steam e per i link di download.
* Supporto Link Diretti: Funziona solo con link diretti che supportano le informazioni header, ma è possibile aggiungere un sistema di ricerca tramite webcrawler.
* Sistema di Aggiornamento Automatico: Include un sistema di aggiornamento automatico, tramite un'applicazione Java esterna non inclusa in questa repository.
* Gestione Download:
- Pausa e Ripresa: Possibilità di mettere in pausa e riprendere i download.
- Coda Ordinabile: Organizzazione dei download in una coda ordinabile.
- Recupero Download: Recupero dei download dal punto in cui erano stati interrotti, anche dopo il riavvio del software.
- Sicurezza: Diversi sistemi per garantire il completamento del download e gestione dei file parziali.
* Gestione File:
- Supporto File .rar: Attualmente supporta solo file .rar con dimensione massima di 5 GB. File più grandi devono essere partizionati in archivi da 5 GB.
- Gestione Partizioni: Le partizioni completamente estratte vengono cancellate per ottimizzare lo spazio su disco.

## Requisiti

* Java 22: Assicurati di avere Java 22 installato sul tuo sistema.
* Windows: Il software è progettato per funzionare su sistemi Windows.
* Google Sheets Access: Il software necessita di accesso alla pagina Google Sheets configurata per il database.

## Limitazioni

Modifica e Distribuzione: Il software è destinato esclusivamente a scopo di dimostrazione. Non deve essere modificato, distribuito o utilizzato al di fuori dell'ambito educativo. Per garantire la sicurezza, la classe launcher è stata rimossa.

## Contributi

Poiché il software è destinato esclusivamente a scopo di esercizio e non deve essere modificato, al momento non accettiamo contributi esterni.

Per ulteriori informazioni, supporto o domande, puoi contattarci a @mm_winther su Discord.
