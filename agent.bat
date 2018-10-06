echo OFF

SET SERVER_PORT=9997
SET CONFIG_FILE=config\service-config.json
SET AGENT_CONFIG_FOLDER=config\agents

SET LOGS_FOLDER=logs

REM ==============================
REM DON'T TOUCH ANYTHING BELOW
REM ==============================

SET JAR=agent.jar

REM ------------------------------
REM Persistence
REM ------------------------------
SET PERSISTENCE_FILE=config\db\thing.db
REM ------------------------------

echo ON
echo "starting agent"


REM DEL /F %DEFAULT_LOG%

java -Xms4096m -Xmx15360m -Dservice.config=%CONFIG_FILE% -Dagents.config=%AGENT_CONFIG_FOLDER% -Dserver.port=%SERVER_PORT% -Dpersistence.file=%PERSISTENCE_FILE% -Dlogs.folder=%LOGS_FOLDER% -jar %JAR%  >> %LOGS_FOLDER%\agent.log 2>&1
echo agent started

