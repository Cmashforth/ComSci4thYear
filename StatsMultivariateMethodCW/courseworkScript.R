rm(list=ls())
load("C:/Users/Chris/code/ComSci4thYear/StatsMultivariateMethodCW/MM2017Project.RData")

data = HTRU.1

#######Split the Data into Test,Train and Validation##################
labels = data$Class
data = data[,-9]

n = nrow(data)
ind1 = sample(c(1:n),round(n/2))
ind2 = sample(c(1:n)[-ind1], round(n/4))
ind3 = setdiff(c(1:n), c(ind1,ind2))

train.data  = data[ind1,]
train.label = labels[ind1]

valid.data = data[ind2,]
valid.label = labels[ind2]

test.data = data[ind3,]
test.label = labels[ind3]

save(train.data,train.label,valid.data,valid.label,test.data,test.label, file = "data.txt")

########Data is saved, must be read in from now on##################

######Regression########
res.lm = lm(train.label~.,data=train.data)
pred.train = ifelse(res.lm$fitted.values <= 0.5,0,1)
table(train.label,pred.train)
correct.class.train = sum(diag(table(train.label,pred.train)))/nrow(train.data)

res.valid = merge(valid.data,test.data, all = TRUE)
res.labels = c(valid.label,test.label)

n.valid = nrow(res.valid)
yhat.valid = cbind(rep(1,n.valid),data.matrix(res.valid)) %*% res.lm$coefficients
yhat.valid = predict(res.lm,res.valid)
pres.valid = ifelse(yhat.valid<= 0.5,0,1)
table(res.labels,pres.valid)

correct.class.valid = sum(diag(table(res.labels,pres.valid)))/nrow(res.valid)

######Knn############
library(class)
corr.class.rate = rep(NA,20)
for(k in 1:20){
  pred = knn(train.data,valid.data,train.label,k=k)
  corr.class.rate[k] = sum(pred == valid.label)/nrow(valid.data)
  
}

plot(1:20,corr.class.rate, type = "l", xlab = "k")
max(corr.class.rate)
best.k = which.max(corr.class.rate)
pred = knn(train.data,test.data,train.label, k = best.k)
sum(pred == test.label)/nrow(test.data)

#####CVA###############
library(MASS)
train.lda = lda(train.data,train.label,prior = c(0.5,0.5))
plot(train.lda)

valid.lda = predict(train.lda,newdata = valid.data)
names(valid.lda)

xtab = table(valid.label,valid.lda$class)
xtab

1 - sum(diag(xtab))/sum(xtab)

test.lda = predict(train.lda,newdata = test.data)
xtab = table(test.label,test.lda$class)
1 - sum(diag(xtab))/sum(xtab)

#######LDA################
train.lda = lda(train.data,train.label)
plot(train.lda)

valid.lda = predict(train.lda,valid.data)
xtab = table(valid.label,valid.lda$class)
xtab

valid.lda$posterior[valid.label != valid.lda$class,]
1 - sum(diag(xtab))/sum(xtab)

test.lda = predict(train.lda,test.data)
xtab = table(test.label,test.lda$class)
xtab
1 - sum(diag(xtab))/sum(xtab)

##########QDA#################
train.qda = qda(train.data,train.label)

valid.qda = predict(train.qda,valid.data)

xtab = table(valid.label,valid.qda$class)
xtab

1 - sum(diag(xtab))/sum(xtab)

test.qda = predict(train.qda,test.data)

xtab = table(test.label,test.qda$class)
xtab
1 - sum(diag(xtab))/sum(xtab)


#########Trees#############
set.seed(126)
library(rpart)
train.tree = cbind(train.label,train.data)
valid.tree = cbind(valid.label,valid.data)


train.rp = rpart(train.label~., data = train.tree, method = "class")

train.rp
plot(train.rp)
text(train.rp)
plotcp(train.rp)

valid.rp = predict(train.rp,valid.tree, type = "class")
xtab = table(valid.label,valid.rp)
xtab
1 - sum(diag(xtab))/sum(xtab)

