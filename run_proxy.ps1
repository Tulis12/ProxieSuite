mvn package
cd .\serverVelocity
try
{
    java -jar velocity.jar
} finally
{
    cd ..
}
