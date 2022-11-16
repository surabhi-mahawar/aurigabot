echo "==> Ensuring .bashrc exists and is writable"
touch ~/.bashrc

echo "Please provide the telegram bot username"
read TELEGRAM_BOT_USERNAME
echo "Please provide the telegram bot token"
read TELEGRAM_BOT_TOKEN
echo ""
# export env variables
sed -i "s|TELEGRAM_BOT_USERNAME=.*|TELEGRAM_BOT_USERNAME=${TELEGRAM_BOT_USERNAME}|g" .env
sed -i "s|TELEGRAM_BOT_TOKEN=.*|TELEGRAM_BOT_TOKEN=${TELEGRAM_BOT_TOKEN}|g" .env

if [ -x "$(command -v docker)" ]; then
    echo "Docker already available"
    echo ""
else
    echo "Installing Docker"
    echo ""
    #  Install Docker
    curl -fsSL https://get.docker.com -o get-docker.sh
    # DRY_RUN=1 sh ./get-docker.sh
fi
if [ -x "$(command -v docker)" ]; then
    echo "Docker Compose already available"
    echo ""
else
    echo "Installing docker-compose"
    echo ""
    sudo curl -L "https://github.com/docker/compose/releases/download/1.26.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

sudo docker-compose up -d postgresqldb
mvn clean install -DskipTests
#mvn spring-boot:run
docker build -t surabhidocker28/aurigabot .
docker run --env-file .env --network=aurigabot_mynet -d -p8080:8080 surabhidocker28/aurigabot