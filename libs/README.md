this file will need to be manually installed int maven using the following command from inside the libs folder(I recommend using git bash):
mvn install:install-file -Dfile=authlib-5.0.47-graves.jar -DgroupId=com.mojang -DartifactId=authlib -Dversion=5.0.47-graves -Dpackaging=jar  
this file is a modified version of authlib that specifically allows backwards compatibility with older authlib versions specifically in graves plugins