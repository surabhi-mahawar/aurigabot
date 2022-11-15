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

sudo mvn clean install -DskipTests
sudo docker build -t surabhidocker28/aurigabot:v1 .
sudo docker-compose up -d aurigabotdb aurigabot