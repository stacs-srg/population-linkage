Umea data on manifesto

Secure home: /home/secure/

Store root: /home/secure/storr-repos

Git working root: /home/secure/git/working-versions/population-linkageRecipe

To update umea data:

/home/secure/git/working-versions/population-linkageRecipe/src/main/scripts/install/manifesto/install_umea_data.sh

Update project:

cd /home/secure/git/working-versions/population-linkageRecipe
git pull
mvn -U clean compile

to run Java: e.g.

cd /home/secure/git/working-versions/population-linkageRecipe

runJava.sh uk.ac.standrews.cs.population_linkage.experiments.UmeaBitBlasterSiblingBundling

runJava.sh is in /home/secure/bin and has all the maven runes.

Application properties for project is in /home/secure/git/working-versions/population-linkageRecipe/properties/application.properties



