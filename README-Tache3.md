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


    
        
