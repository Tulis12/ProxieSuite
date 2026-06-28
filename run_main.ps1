mvn package
cd .\serverMain
try
{
    java -jar server.jar -nogui
} finally
{
    cd ..
}
