IFT3913 --- Tache2

Génération de tests avec un modèle de langage et évaluation par tests de
mutation

Étude de cas : Apache Tika --- module tika-core

Équipe : Wendkuni Reine Rosine Guinguiere et Alison Deblois

Date de préparation : 13 octobre 2026 \| Version : brouillon
collaboratif

Statut : section Metadata documentée ; section ConcurrentUtils

Note de méthode : les résultats ci-dessous proviennent des exécutions
locales communiquées pendant le travail. Les parties non encore
vérifiées sont explicitement signalées et ne doivent pas être présentées
comme des résultats obtenus.

# Résumé

Ce travail examine l'utilisation de ChatUniTest, associé à un modèle de
langage local, pour assister la création de tests unitaires dans Apache
Tika. La classe org.apache.tika.metadata.Metadata a été étudiée à l'aide
de JUnit 5, JaCoCo et PIT. ChatUniTest a proposé des scénarios, mais n'a
pas produit de tests directement compilables lors de l'exécution
observée. Après correction et conception manuelles, cinq tests ont été
ajoutés. Sur le périmètre PIT configuré, le nombre de mutations
éliminées est passé de 66 à 71, tandis que les mutations survivantes
couvertes sont passées de cinq à zéro. La couverture des lignes mesurée
par PIT est restée à 47 %, avec 70 mutations non couvertes. La
contribution RegexUtils sera complétée séparément.

# 1. Objectifs et périmètre

Expérimenter la génération de tests unitaires assistée par ChatUniTest
et un modèle local.

Vérifier la compilation, l'exécution et la pertinence des assertions des
tests proposés.

Comparer les résultats des tests de mutation avant et après ajout de
tests.

Identifier les limites de l'outil et documenter les interventions
humaines.

Projet étudié : Apache Tika, module tika-core. Classe analysée dans la
première contribution : org.apache.tika.metadata.Metadata. Deuxième
contribution : org.apache.tika.utils.ConcurrentUtils

# 2. Environnement et outils

  -----------------------------------------------------------------------
Élément                             Configuration utilisée / observée
  ----------------------------------- -----------------------------------
Projet                              Apache Tika, tika-core, version
locale 4.0.0-SNAPSHOT

Langage / build                     Java ; Maven

Tests                               JUnit 5

Génération assistée                 ChatUniTest Maven plugin 1.4.1

Modèle                              Ollama, qwen2.5-coder:3b (local),
quwen2.5-code :7b (local)

Analyse des mutations               PIT Maven 1.22.0 ;
pitest-junit5-plugin 1.2.3

Couverture                          JaCoCo 0.8.15 ; rapport PIT

Environnement                       macOS, VS Code ; service Ollama à
127.0.0.1:11434, windows, Intellij
  -----------------------------------------------------------------------

La configuration ChatUniTest utilisée pour Metadata demandait deux
tests, au plus deux cycles de correction, sans parallélisme, avec une
sortie dans target/chatunitest-generated. PIT ciblait Metadata et les
classes TestMetadata et MetadataHashCodeGeneratedTest.

# 3. Démarche expérimentale --- Metadata

## 3.1. Situation initiale

Les tests existants de la classe TestMetadata ont été exécutés avec
succès (18 tests lors de l'exécution initiale rapportée). L'analyse
JaCoCo de Metadata indiquait environ 61 % de couverture des instructions
et 57 % des branches. Les chiffres PIT ci-dessous correspondent au
périmètre de classes mutées et de tests explicitement configuré ; ils ne
sont pas directement interchangeables avec les pourcentages JaCoCo.

## 3.2. Tentative de génération avec ChatUniTest

ChatUniTest a été exécuté sur la méthode hashCode de Metadata avec le
modèle local qwen2.5-coder:3b. L'exécution a duré environ 8 min 51 s.
Malgré un BUILD SUCCESS affiché par Maven, aucun test généré directement
exploitable et compilable n'a été obtenu. Les journaux d'essai faisaient
apparaître des échecs de compilation, un dépassement de limite de jetons
de prompt et une erreur de délai réseau/serveur
(SocketTimeoutException).

Exemples d'erreurs observées dans les propositions : appel à
metadata.put(String, String\[\]) inexistant, appel à
metadata.set(String, String, HashMap) inexistant, signature erronée pour
un calcul auxiliaire de hashCode, et imports Mockito inadaptés à la
configuration. Le statut Maven ne suffisait donc pas à attester de la
réussite de la génération de tests.

## 3.3. Construction et validation des tests

Une nouvelle classe MetadataHashCodeGeneratedTest.java a été créée. Les
scénarios initiaux autour de hashCode ont été adaptés et corrigés
manuellement ; d'autres tests ont été écrits après consultation des
mutants survivants. Les cinq tests sont les suivants :

  ------------------------------------------------------------------------------
Test                                       Intention / oracle
  ------------------------------------------ -----------------------------------
testEmptyMetadataHashCode                  Vérifier la valeur du hashCode pour
un objet Metadata vide.

testHashCodeChangesWhenValueChanges        Vérifier que le changement d'une
valeur modifie le hashCode dans le
cas choisi.

testEqualMetadataHaveSameHashCode          Vérifier l'égalité des hashCode de
deux objets de même contenu.

testHashCodeUsesXorAndAddition             Comparer le résultat à une
expression attendue fondée sur XOR
et addition, afin de détecter
certaines mutations arithmétiques.

testCompositePropertySetsPrimaryProperty   Vérifier que set sur une propriété
composite affecte bien la propriété
principale et l'alias secondaire.
  ------------------------------------------------------------------------------

Commande de vérification exécutée depuis tika-core :

mvn -Dtest=MetadataHashCodeGeneratedTest -DskipTests=false
-Dmaven.test.skip=false test

Résultat final observé : Tests run: 5, Failures: 0, Errors: 0, Skipped:
0 ; BUILD SUCCESS.

# 4. Analyse des mutations avec PIT

## 4.1. Résultats quantitatifs

  --------------------------------------------------------------------------
Indicateur     Avant ajout    Après 3 tests  Après 4 tests  Après 5 tests
  -------------- -------------- -------------- -------------- --------------
Mutants        141            141            141            141
générés

Mutants        66             68             70             71
éliminés

Mutants        5              3              1              0
survivants

Mutants sans   70             70             70             70
couverture

Score de       47 %           48 %           50 %           50 %
mutation                                                    
(arrondi)

Force des      93 %           96 %           99 %           100 %
tests                                                       
(arrondie)

Couverture de  47 %           47 %           47 %           47 %
lignes PIT
  --------------------------------------------------------------------------

Le score de mutation global final est de 71/141, soit environ 50,4 % (50
% affiché par PIT). La force des tests est de 71/(141 − 70) = 100 % :
tous les mutants couverts ont été éliminés. Cela ne signifie ni que
toute la classe est couverte, ni que tous les défauts possibles seraient
détectés.

## 4.2. Analyse des mutants survivants

  ------------------------------------------------------------------------------------
Emplacement             Mutation observée                    Réponse apportée
  ----------------------- ------------------------------------ -----------------------
Metadata.java, ligne    Remplacement de l'addition par une   Ajout d'un oracle
748                     soustraction dans hashCode           calculant explicitement
la somme attendue.

Metadata.java, ligne    Remplacement du résultat de hashCode Assertions sur un
750                     par 0                                hashCode non nul ou
dépendant du contenu.

Metadata.java, ligne    Remplacement du résultat auxiliaire  Comparaison à la valeur
754                     par 0 ; remplacement de XOR par AND  attendue avec XOR.

Metadata.java, ligne    Suppression de l'appel               Test ciblé sur une
480                     set(property.getPrimaryProperty(),   propriété composite
value)                               avec noms distincts
pour la propriété
principale et l'alias.
  ------------------------------------------------------------------------------------

Les résultats PIT ont progressé par itérations : les trois premiers
tests ont éliminé deux mutants supplémentaires, le quatrième deux
autres, puis le cinquième le dernier mutant survivant. Le test composite
existant ne détectait pas cette dernière mutation dans la configuration
analysée ; le nouveau test isole explicitement la propriété principale.

## 4.3. Limites de l'évaluation

70 mutants restent non couverts : la couverture de lignes PIT demeure à
123/260 (47 %).

La configuration cible une classe précise et un sous-ensemble de tests ;
les résultats ne décrivent pas la qualité globale de tous les modules
Apache Tika.

Un test de hashCode reposant sur une expression interne attendue peut
être efficace contre certains mutants, mais plus fragile face à un
refactoring légitime.

La génération n'a pas fourni de tests prêts à l'emploi : il serait
incorrect de présenter les cinq tests comme générés automatiquement sans
intervention humaine.

# 5. Analyse critique de ChatUniTest et des oracles

L'expérience montre une différence importante entre produire du texte
ressemblant à un test et obtenir un test utile. Les méthodes inventées
par le modèle ne correspondaient pas à l'API réelle de Metadata. La
correction humaine a exigé la consultation des signatures Java, le choix
d'assertions valides et l'exécution effective de JUnit. Les tests de
mutation ont ensuite servi de retour empirique : chaque mutant survivant
a suggéré un comportement qui n'était pas suffisamment contraint par les
assertions existantes.

La valeur de l'outil, dans cette expérience, réside surtout dans
l'amorce de scénarios. La qualité finale provient de la combinaison
entre revue humaine, tests exécutables et analyse PIT. Les limites
constatées peuvent dépendre du modèle local utilisé, de la taille du
contexte, de la configuration et du temps de réponse ; elles ne
démontrent pas que ChatUniTest échoue dans tous les environnements.

# 6. Contribution 2 --- MimeTypesFactory

## 6.1. Classe et justification du choix

Dans cette classe, on retrouve une couverture incomplète du code. Ceci
est évalué par Jacoco, et les résultats étaient de 68% pour les
instructions et 81% pour les branches. De plus, l'analyse de mutations
avec PIT a révelé qu'on avait 9/20 mutations. Comme la classe était
beaucoup plus petite que celle de Metadata, on peut déterminer si
ChatUnitTest se livre de façon différente selon la classe pour laquelle
il génère.

------------------------------------------------------------------------

## 6.2. Configuration de ChatUniTest et génération

La configuration de ChatUniTest était simple, on ajoute tout simplement
le plug-in dans le fichier pom.xml de notre projet maven. Cependant,
c'est l'utilisation et le build qui ont compliqué les choses, car j'ai
eu plusieurs problèmes de résolutions de dépendances à régler.
Finalement, après avoir installé le tout correctement, la génération de
tests était longue, et l'erreur de « timeout » était récurrente. Il
semble que le modèle prenait trop de temps àr. ChatUniTest était
configuré pour utiliser qwen2.5-code :7b pour la génération de tests.

------------------------------------------------------------------------

## 6.3. Tests produits, corrections et oracles

Les tests produits n'ont nécessité aucune correction.

  -----------------------------------------------------------------------
Test                                Intention / oracle
  ----------------------------------- -----------------------------------
testCreate                          Vérifier qu'une factory est bien
crée

testCreateWithDocument              Vérifier qu'une factory est créée
quand on utilise un document

testCreateWithInputStreams          Vérifier qu'une factory est créée
quand on utilise un Input Stream

testCreateWithURLs                  Vérifier qu'une factory est créée
quand on utilise un URL
  -----------------------------------------------------------------------

Bien que le test test_create compile au premier essai, on peut critiquer
son utilité. Il ne vérifie pas que la tâche s'exécute correctement ou
que le résultat est le bon, car on mock la création d'un factory de
toute manière. On augmente la couverture, mais la qualité du test n'est
pas assurée.

------------------------------------------------------------------------

## 6.4. Couverture initiale et finale

Avant la génération, Jacoco indiquait une couverture de 68% des
instructions et 81% des branches. Après avoir testé avec ce que
ChatUniTest a généré, on obtient une couverture de 78% des instructions
et encore 81% des branches. Ceci est une augmentation intéressante, mais
on ne peut pas conclure que la qualité s'est améliorée pour autant. Les
tests couvrent plus de code, mais la détection de vraies failles reste
limitée. Avec PIT, on avait un score de mutation de 45% avant l'ajout
des tests et après on a 55%.

------------------------------------------------------------------------

## 6.5. PIT avant/après et analyse des mutants

Avant la génération de tests, on a un score PIT de 45% et on compte 9
mutants éliminés. Après la génération, on obtient 55% et un changement
dans les mutants survivants/éliminés, etc. (voir tableau)

------------------------------------------------------------------------

## 6.6. Difficultés rencontrées et enseignements

La plus grande difficulté rencontrée a été l'intégration de ChatUnitTest
dans Tika, un projet déjà existant et qui repose sur plusieurs
dépendances et plug-ins. Cela m'a notamment permis de mieux comprendre
la gestion de modules et dépendances dans un gros projet. D'un autre
côté, la génération du test de MimeTypesFactory montre que l'analyse
d'un test est absolument nécessaire pour bien comprendre sa pertinence
et son utilité dans la suite. Même si le test augmente la couverture ou
baisse les mutations, la qualité n'est pas garantie.

------------------------------------------------------------------------

# 7. Synthèse comparative des deux contributions (à finaliser)

Critère                            Metadata                         MimeTypesFactory
  ---------------------------------- -------------------------------- ------------------------
Tests ajoutés                      5                                4 (avant arrêt manuel)
Tests réussis                      5/5                              4/4
Mutants éliminés avant / après     66 / 71                          9/11
Mutants survivants avant / après   5 / 0                            2/
Mutants non couverts               70                               9
Score PIT avant / après            47 % / 50 %                      45%/55%
Intervention humaine               Corrections + tests ciblés PIT   Aucune

# 8. Reproductibilité et éléments de preuve

Exemples de commandes utilisées dans le module tika-core :

mvn -Dtest=MetadataHashCodeGeneratedTest -DskipTests=false
-Dmaven.test.skip=false test

mvn org.pitest:pitest-maven:1.22.0:mutationCoverage

mvn io.github.ZJU-ACES-ISE:chatunitest-maven-plugin:1.4.1:method
'-DselectMethod=org.apache.tika.metadata.Metadata#hashCode'
-DnoExecution=true

Fichiers et artefacts :
tika-core/src/test/java/org/apache/tika/metadata/MetadataHashCodeGeneratedTest.java
; tika-core/pom.xml ; rapports locaux dans tika-core/target/pit-reports/
; journaux ChatUniTest locaux dans /tmp/chatunitest-info/. Les
répertoires target et /tmp ne sont pas inclus dans le commit présenté.

Dépôt d'équipe : https://github.com/bbroux/tika ; branche de
contribution : tests-metadata ; commit : 6104e36a3. Une Pull Request a
été créée vers main, sans conflit signalé par GitHub lors de la capture
fournie. Numéro et URL définitive de la PR :
https://github.com/bbroux/tika/pull/1

# 9. Conclusion

Pour Metadata, l'approche combinant génération assistée, revue humaine
et mutation testing a amélioré la capacité de détection des mutants
couverts, de 93 % à 100 % de force des tests. Le score global reste
limité par les zones non couvertes (70 mutants), ce qui montre que
l'amélioration des oracles ne remplace pas l'élargissement de la
couverture. Pour une classe plus petite comme MimeTypesFactory, la
génération de test a fonctionné, mais pas au complet. Il a fallu arrêter
la génération, car ça s'avérait trop long. On a donc jamais eu la
notification du build réussi. Par contre,on a des tests générés quand
même, et une augmentation dans la couverture ainsi que dans le score de
mutation. Les tests compilent après aucune modification. Cependant, les
tests générés ne sont pas très pertinents, on n'obtient pas une
meilleure idée de si la classe fonctionne bien ou non.
