mvn package
cd .\serverLogin
try
{
    java -jar server.jar -nogui
} finally
{
    cd ..
}
