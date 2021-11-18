Usage
=====
* Build the plugin:

        mvn clean install
		
* Copy the plugin into SONARQUBE_HOME/extensions/plugin
* Restart your SonarQube server
* Go to Settings > Quality Profile
* The custom PMD rules are now available
* Activate new rules (see pmd-extension.xml for defined rules list)
* Run a SonarQube analysis to check your code against this coding rule
