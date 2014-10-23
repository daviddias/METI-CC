#!/bin/bash

LAUNCH_CONFIGURATION=weatherAppScalingConf
AUTOSCALING_GROUP=weatherAppScalingGroup

#Tempo entre escalamentos
ASG_COOLDOWN=10
#Periodo em que o load-balancer ignora se a instancia nao responder
ASG_GRACE_PERIOD=60
#Dimensao minima do Grupo
ASG_MIN=2
#Dimensao maxima do Grupo
ASG_MAX=10
#Quantas instancias para incrementar?
ADJUSTMENT_UP=2
#Intervalo entre incrementos
SCALING_POL_COOL_UP=10

#Quantas instancias para decrementos?
ADJUSTMENT_DOWN=-2
#Intervalo entre decrementos
SCALING_POL_COOL_DOWN=10

#Periodo de verificacao
ALARM_PERIOD_UP=60
#Threshold
ALARM_THRESHOLD_UP=50
#Num de avaliacoes necessarias
ALARM_NUM_EVALUATION_UP=1
#Tipo de estatistica SampleCount, Average, Sum, Minimum, Maximum.
ALARM_STATISTIC_UP=Maximum

#Periodo de verificacao
ALARM_PERIOD_DOWN=60
#Threashold
ALARM_THRESHOLD_DOWN=20
#Num de avaliacoes necessarias
ALARM_NUM_EVALUATION_DOWN=1
#Tipo de estatistica  SampleCount, Average, Sum, Minimum, Maximum.
ALARM_STATISTIC_DOWN=Maximum

#Delete old data

as-delete-policy scale-up --auto-scaling-group $AUTOSCALING_GROUP
as-delete-policy scale-down --auto-scaling-group $AUTOSCALING_GROUP
as-delete-auto-scaling-group $AUTOSCALING_GROUP --force-delete
as-delete-launch-config $LAUNCH_CONFIGURATION

sleep 60
#Create new
as-create-launch-config $LAUNCH_CONFIGURATION --region us-west-1 --image-id ami-64e4c421 --instance-type t1.micro
as-create-auto-scaling-group $AUTOSCALING_GROUP 	--launch-configuration $LAUNCH_CONFIGURATION --default-cooldown $ASG_COOLDOWN --grace-period $ASG_GRACE_PERIOD --load-balancers  weatherBalancer --min-size $ASG_MIN --max-size $ASG_MAX --availability-zones us-west-1a --region us-west-1
sleep 5 
ACTION_UP=$(as-put-scaling-policy --auto-scaling-group $AUTOSCALING_GROUP --name scale-up --adjustment $ADJUSTMENT_UP --type ChangeInCapacity --cooldown $SCALING_POL_COOL_UP)
ACTION_DOWN=$(as-put-scaling-policy --auto-scaling-group $AUTOSCALING_GROUP --name scale-down --adjustment=$ADJUSTMENT_DOWN --type ChangeInCapacity --cooldown $SCALING_POL_COOL_DOWN)
sleep 5
echo $ACTION_UP
echo $ACTION_DOWN 
mon-put-metric-alarm --alarm-name CloudWeather-scale-up-alarm --alarm-description "Scale up at $ALARM_THRESHOLD_UP % load" --metric-name CPUUtilization --namespace AWS/EC2 --region us-west-1  --statistic $ALARM_STATISTIC_UP  --period $ALARM_PERIOD_UP --threshold $ALARM_THRESHOLD_UP --comparison-operator GreaterThanThreshold --evaluation-periods $ALARM_NUM_EVALUATION_UP  --unit Percent --dimensions "AutoScalingGroupName= $AUTOSCALING_GROUP" --alarm-actions $ACTION_UP
mon-put-metric-alarm --alarm-name CloudWeather-scale-down-alarm --alarm-description "Scale down at $ALARM_THRESHOLD_DOWN % load"  --metric-name CPUUtilization  --namespace AWS/EC2  --statistic Average  --period $ALARM_PERIOD_DOWN --threshold $ALARM_THRESHOLD_DOWN  --comparison-operator LessThanThreshold --evaluation-periods $ALARM_NUM_EVALUATION_DOWN   --unit Percent --dimensions "AutoScalingGroupName= AUTOSCALING_GROUP" --region us-west-1 --alarm-actions $ACTION_DOWN
