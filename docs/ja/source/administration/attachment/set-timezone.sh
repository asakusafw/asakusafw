#!/bin/sh

echo "Asia/Tokyo" | sudo tee /etc/timezone

yes | sudo apt-get install locales-all
echo "export LANG=ja_JP.UTF-8" >> /home/hadoop/.bashrc

