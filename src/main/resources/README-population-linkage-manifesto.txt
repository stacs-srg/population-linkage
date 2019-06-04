Umea data on manifesto

Secure home: /home/secure/

Store root: /home/secure/storr-repos

Git working root: /home/secure/git/working-versions/population-linkage

To update umea data:

/home/secure/git/working-versions/population-linkage/src/main/scripts/install/manifesto/install_umea_data.sh

Update project:

cd /home/secure/git/working-versions/population-linkage
git pull
mvn -U clean compile

to run Java: e.g.

cd /home/secure/git/working-versions/population-linkage

runJava.sh uk.ac.standrews.cs.population_linkage.experiments.UmeaBitBlasterSiblingBundling

runJava.sh is in /home/secure/bin and has all the maven runes.

Application properties for project is in /home/secure/git/working-versions/population-linkage/properties/application.properties



