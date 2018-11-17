#!/bin/bash
java -cp jade.jar:. jade.Boot -agents "main:MainAgent;fixed:FixedAgent;random:RandomAgent"
