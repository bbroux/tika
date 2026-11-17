## Tache 3

# 1. Ajout d'une Github action pour la vérification automatique de Semgrep 

    J'ai commencé par me rendre au répertoire GitHub de mon projet tika que j'ai fork pour la tâche 2. Ensuite, dans la section "Actions" j'ai voulu ajouter l'action. J'ai recherché Semgrep, et c'était déjà reconnu par Github. Cependant, ils m'ont généré un fichier de configuration automatique qui mentionnait qu'il fallait avoir un compte Semgrep gratuit (ce que je n'avais pas). J'ai été sur le site de Semgrep, je me suis crée un compte gratuit (avec mes identifiants Github), et j'ai ensuite ajouté mon répertoire Github dans mes projets sur le site de Semgrep. C'était assez simple, et immédiatemment j'ai pu ajouter le github action avec un fichier de configuration fourni par Semgrep lui même. J'ai pensé à ne mettre aucun run "scheduled", car je voulais faire les vérifications seulement lors d'un pull request. Finalement je n'ai pas modifié le fichier de configuration par peur de changer quelque chose qui ne fallait pas!Le commit s'est fait directement, c'était très facile et je n'ai pas eu de problèmes. L'action a été exécutée sur le répertoire sans modifications (donc le répertoire après la tâche 2). Ceci était automatique quand l'ajout de l'action a été "commit". Il n'y a eu aucune erreur détectée par Semgrep. Pour vérifier le tout, j'ai commencé à écrire ce README et je l'ai push. J'ai ensuite fait une pull request vers mon main pour m'assurer que l'action se déroulerait automatiquement et qu'il n'y avait pas de problème. Heureusement, c'était le cas et ce Semgrep #2 a aussi terminé sans "blocking findings". Par contre, sur Semgrep.dev, on voit un rapport détaillé où certaines vulnérabilités sont soulignées, mais on a toujours la mention "no blocking findings" pour résumer le run. J'ai essayé de trouver des détails aussi pertinents sur le repo Github directement, mais je n'ai pas trouvé. Par contre, je ne suis pas certaine si cette action va faire fail le build ou non si Semgrep découvre une vulnérabilité bloquante. C'est ce qu'on va voir au numéro 2.  

# 2. Ajout de vulnérabilités reconnaissables par Semgrep

    Dans la documentation de Semgrep, je me suis rendue au "Registry", qui nomme les règles de base utilisées pour l'analyse de Semgrep (il existe aussi la possibilité de définir ses propres règles). On a un total de 241 règles pour le langage Java, et plusieurs autres langages ont leurs règles de base déterminées par Semgrep. Voici les types de règles sélectionnées pour tester le fonctionnement de Semgrep face aux vulnérabilités détectées. Je vais ajouter des erreurs de type "critical" ou "high", car mon policy pour Semgrep est d'échouer si on a des erreurs critiques ou importantes détectées. J'ai configuré ceci dans le Semgrep cloud. 

    1. Utilisation d'un SaxParser sans les options de sécurité
        url de la règle : https://semgrep.dev/r?q=java.lang.security.xxe.saxparserfactory-xxe.saxparserfactory-xxe

        Essai 1 (PR#5 - add v1) : Ajouter une modification dans tika-core/main/src/java/XMLReaderUtils.java       
        -> Résultat : Cette modification n'a pas mené à une nouvelle détection par Semgrep, car je n'ai pas changé les configurations du parser du tout.

        Essai 2 (PR#5 - add v1-try2) : Ajouter une modification dans tika-core/main/src/java/XMLReaderUtils.java dans la fonction getSaxParserFactory(). Je retire les features de sécurité recommandés selon la doc semgrep. 
        -> Résultat: La modification ne fonctionne toujours pas, les erreurs étaient présentes et flaggées, mais ne brisaient pas le build avant car la policy semgrep n'était pas bloquante. Maintenant, avec l'ajout de la policy, ces erreurs sont toujours détectées et deviennent blocantes, mais ce n'est pas à cause de la modification dans le code, mais à cause de celle dans la configuration de semgrep. 

        Il est difficile de trouver une modification qui causerait un problème et de l'ajouter, car la plupart sont dépendantes de certaines technologies que le projet tika n'utilise pas (ex. SQL). Je vais donc élargir mes recherches à toutes les erreurs possibles  et je vais indiquer de briser le build, c'est-à-dire qu'on inclut les vulnérabilités "high risk" et "critical" mais aussi les plus minimes comme "info" et "error". Je vais changer ceci dans la configuration semgrep en ligne. Par contre, c'est à noter que les vulnérabilités qui étaient présentes au début, lorsqu'on a ajouté semgrep, seront toutes flaggées. On en avait 98 de détectées, aucune "blocking". Maintenant qu'on élargit, nos 98 vulnérabilités détectées dans le code seront "blocking". On cherche une run qui détectera plus que 98 erreurs, signalant une nouvelle détection par semgrep qu'on avait pas vue avant. 

        J'ai continué à chercher, mais il est compliqué d'intégrer une nouvelle vulnérabilité qui concorde avec le code existant. Oui, il y a plusieurs règles Semgrep utilisées en Java, mais lorsque je recherche dans le répertoire du projet, les règles ne sont pas applicables (technologie pas utilisée, et il faut modifier du code existant et non en ajouter). Sinon, les règles qui seraient applicables sont déjà détectées par Semgrep et l'étaient lorsqu'on a fait le premier run de Semgrep à son initialisation dans les github actions. Le seul changement que je pense qui pourrait faire réagir Semgrep c'est l'ajout d'une dépendance vulnérable. On passe à l'ajout de la vulnérabilité 2 (ajout d'une dépendance)
    
    2. Ajout d'une dépendance qui a une vulnérabilité connue dans tika-core

        Selon mes recherches, la version 1.9 de commons-text comprend des vulnérabilités importantes et est flaggée comme tel. On va l'ajouter dans le graph maven de tika-core (en ajoutant dans tika-core/pom.xml), et vérifier si le tout est détecté par Semgrep lorsqu'on fait un push. 

        https://commons.apache.org/proper/commons-text/security.html

        Je l'ai fait, mais le build de tika ne fonctionne plus car il y a un problème de résolution des dépendances. Une autre dépendance crée la dépendance transitive de commons-text 1.11, donc il y a un conflit de version avec le commons-text 1.9 qu'on a tenté d'ajouter pour soulever une vulnérabilité. Je l'ai donc retirée pour que le build fonctionne. 

        Pour quand même tenter ma chance et voir si cette vulnérabilité serait reconnue par Semgrep, j'ai mit la dépendance vulnérable apache.shiro.shiro-core version 1.8.0. Par contre, malgré plusieurs tentatives Semgrep ne reconnait pas les dépendances du projet, donc il ne reconnait pas le problème. 


    
        

# 3. Solution finale et validation

## 3.1 Configuration finale de Semgrep dans GitHub Actions

Après les premières expérimentations avec Semgrep Cloud, nous avons choisi de mettre en place une comparaison explicite entre les vulnérabilités présentes avant et après les modifications.

Le workflow `.github/workflows/semgrep.yml` est déclenché lors des pull requests, des pushs vers `main` ou `master`, ainsi que manuellement.

Il utilise l'image Docker `semgrep/semgrep:1.185.0` et exécute les étapes suivantes :

1. Récupération du dépôt Git avec son historique complet (`fetch-depth: 0`).
2. Configuration de `safe.directory` afin d'autoriser les commandes Git dans le conteneur.
3. Détermination du commit de référence selon le type d'événement GitHub.
4. Analyse du code actuel avec `semgrep scan --config auto`.
5. Analyse du commit de référence dans un répertoire Git temporaire.
6. Comparaison des résultats avec `scripts/compare_semgrep.py`.

Le script compare les occurrences d'alertes en fonction de leur règle Semgrep et de leur fichier. Il tient compte du nombre d'occurrences afin d'éviter de considérer comme nouvelles des alertes simplement déplacées dans le fichier à cause de changements de numéros de ligne.

Si de nouvelles alertes apparaissent, le script retourne un code de sortie non nul. GitHub Actions considère alors le contrôle comme échoué, ce qui permet de signaler automatiquement l'introduction de vulnérabilités.

## 3.2 Introduction de sept catégories de vulnérabilités

Nous avons ajouté sept fichiers Java dans le répertoire :

`tika-core/src/main/java/org/apache/tika/securitydemo/`

Ces fichiers contiennent volontairement des pratiques non sécuritaires à des fins pédagogiques. Ils ne sont pas destinés à une utilisation en production.

### Vulnérabilité 1 — Exécution de commandes potentiellement dangereuses

- Fichier : `CommandInjectionDemo.java`
- Type : utilisation dangereuse de commandes système.
- Explication : une commande est construite à partir d'une entrée externe avant d'être transmise à `ProcessBuilder`. Cette pratique peut devenir dangereuse si les données ne sont pas contrôlées, notamment lorsqu'un interpréteur de commandes est utilisé.
- Règle Semgrep : `java.lang.security.audit.command-injection-process-builder.command-injection-process-builder`
- Résultat : 1 nouvelle alerte.

### Vulnérabilité 2 — Vérification TLS insuffisante

- Fichier : `InsecureHostnameVerifierDemo.java`
- Type : contournement de la vérification du nom d'hôte.
- Explication : le vérificateur accepte systématiquement le nom d'hôte présenté par le serveur. Cela peut permettre d'accepter un certificat associé à un serveur différent de celui attendu.
- Règles Semgrep :
  - `java.lang.security.audit.crypto.ssl.insecure-hostname-verifier.insecure-hostname-verifier`
  - `problem-based-packs.insecure-transport.java-spring.bypass-tls-verification.bypass-tls-verification`
- Résultat : 2 nouvelles alertes.

### Vulnérabilité 3 — Communication réseau non chiffrée

- Fichier : `UnencryptedSocketDemo.java`
- Type : utilisation d'une connexion réseau sans chiffrement.
- Explication : l'utilisation d'un socket TCP classique ne garantit pas la confidentialité des données transmises. Sans mécanisme de chiffrement supplémentaire, ces données peuvent être interceptées.
- Règle Semgrep : `java.lang.security.audit.crypto.unencrypted-socket.unencrypted-socket`
- Résultat : 1 nouvelle alerte.

### Vulnérabilité 4 — Mode de chiffrement AES-ECB

- Fichier : `WeakEncryptionDemo.java`
- Type : utilisation d'un mode de chiffrement non recommandé.
- Explication : le mode ECB chiffre indépendamment les blocs identiques, ce qui peut révéler des structures dans les données.
- Règle Semgrep : `java.lang.security.audit.crypto.use-of-aes-ecb.use-of-aes-ecb`
- Résultat : 1 nouvelle alerte.

### Vulnérabilité 5 — Algorithme de hachage SHA-1

- Fichier : `WeakHashDemo.java`
- Type : utilisation d'un algorithme de hachage cryptographique affaibli.
- Explication : SHA-1 présente des faiblesses connues en matière de résistance aux collisions et ne convient plus aux usages exigeant cette propriété de sécurité.
- Règle Semgrep : `java.lang.security.audit.crypto.use-of-sha1.use-of-sha1`
- Résultat : 1 nouvelle alerte.

### Vulnérabilité 6 — Injection SQL

- Fichier : `SQLInjectionDemo.java`
- Type : construction d'une requête SQL par concaténation.
- Explication : une entrée utilisateur est incorporée directement dans une requête SQL. Un attaquant pourrait modifier la structure de la requête si cette entrée n'est pas correctement traitée.
- Règles Semgrep :
  - `java.lang.security.audit.formatted-sql-string.formatted-sql-string`
  - `java.lang.security.audit.sqli.jdbc-sqli.jdbc-sqli`
- Résultat : 2 nouvelles alertes.

### Vulnérabilité 7 — Analyse XML non sécurisée (XXE)

- Fichier : `XXEDemo.java`
- Type : XML External Entity (XXE).
- Explication : un parseur XML est créé sans désactiver explicitement les constructions XML dangereuses. Selon sa configuration, un document malveillant pourrait provoquer la résolution d'entités externes.
- Règle Semgrep : `java.lang.security.audit.xxe.documentbuilderfactory-disallow-doctype-decl-missing.documentbuilderfactory-disallow-doctype-decl-missing`
- Résultat : 1 nouvelle alerte.

## 3.3 Comparaison des résultats Semgrep

Nous avons effectué une analyse avant l'introduction des vulnérabilités, puis une nouvelle analyse après leur ajout.

| Indicateur | Résultat |
|---|---:|
| Alertes initiales | 83 |
| Alertes après modification | 92 |
| Nouvelles alertes | 9 |
| Fichiers Java de démonstration | 7 |

Les neuf nouvelles alertes correspondent aux sept catégories de vulnérabilités introduites. Deux fichiers déclenchent chacun deux règles Semgrep distinctes.

La commande de validation utilisée est :

```bash
python3 scripts/compare_semgrep.py semgrep-avant.json semgrep-apres-final.json
```

### Résultat de la comparaison

- Alertes initiales : **83**
- Alertes finales : **92**
- Nouvelles alertes : **9**

Le script retourne un code de sortie 1 lorsqu'il détecte
de nouvelles vulnérabilités.

## 3.4 Validation sur GitHub Actions

La pull request #17 a été créée dans le dépôt bbroux/tika :
https://github.com/bbroux/tika/pull/17

Le workflow Semgrep compare le code de référence au code modifié.

La première exécution a rencontré une erreur Git
« dubious ownership », corrigée avec safe.directory.

Après correction, GitHub Actions a confirmé :
- 83 alertes de référence ;
- 92 alertes actuelles ;
- 9 nouvelles alertes ;
- un échec du contrôle avec le code de sortie 1.

Cela démontre que le workflow bloque les nouvelles alertes.


## 3.5 Action réutilisable Rickroll

Nous avons créé une action composite réutilisable dans
`.github/actions/rickroll/action.yml`.

Cette action affiche un message humoristique et un lien
vers la vidéo « Never Gonna Give You Up ».

Elle est intégrée au workflow
`.github/workflows/main-jdk17-build.yml`,
dans les jobs de compilation et de tests E2E.

Sur GitHub Actions, les étapes suivantes ont réussi :
- `Rickroll if build tests fail`
- `Never Gonna Give You Up`

L'action est donc correctement intégrée et exécutable.

Cependant, l'échec Maven observé provenait du contrôle
Apache RAT et non d'un test unitaire. Le déclenchement
après un échec de test reste à valider spécifiquement.

## 3.6 Compilation et problème préexistant

La compilation locale du module tika-core a réussi avec :

`mvn -pl tika-core -am compile -DskipTests -q`

Sur GitHub Actions, le build complet a échoué au contrôle
Apache RAT à cause du fichier `tika-core/README.md`.

Ce fichier contient le rapport de la tâche 2 et ne possède
pas l'en-tête de licence Apache attendu.

Nous avons vérifié que ce fichier était déjà présent sur
la branche main et qu'il était identique dans notre branche.

Nous avons choisi de le conserver sans modification.

## 3.7 Conclusion

Nous avons intégré un contrôle de sécurité automatisé
au projet Apache Tika.

Sept catégories de vulnérabilités ont été introduites,
produisant neuf nouvelles alertes Semgrep.

La comparaison avant/après et le blocage automatique
ont été validés sur GitHub Actions.

Nous avons également créé une action Rickroll réutilisable
et confirmé son exécution.

Ces vulnérabilités sont intentionnelles et servent
exclusivement à la démonstration pédagogique
dans le cadre du cours IFT3913.
