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

load(file = "data.txt")

######Regression########
res.lm = lm(train.label~.,data=train.data)
pred.class = predict(res.lm, valid.data)
pred.class = ifelse(pred.class >= 0.5, pred.class <- 1, pred.class <- 0)
corr.class.rate = sum(valid.label == pred.class)/nrow(valid.data)

table(pred.class, valid.label)


######Knn############
library(class)
corr.class.rate = rep(NA,50)
for(k in 1:50){
  pred = knn(train.data,valid.data,train.label,k=k)
  corr.class.rate[k] = sum(pred == valid.label)/nrow(valid.data)
}

corr.class.rate

plot(1:50,corr.class.rate, type = "l", xlab = "Choice of K", ylab = "Classification Rate")

pred.best = knn(train.data,valid.data,train.label,k=21)
table(pred.best, valid.label)

corr.class.rate

#####CVA###############
library(MASS)
train.lda = lda(train.data,train.label,prior = rep(0.5,2))
train.lda
plot(train.lda)

valid.lda = predict(train.lda,newdata = valid.data)
names(valid.lda)
valid.lda
xtab = table(valid.label,valid.lda$class)
xtab

1 - sum(diag(xtab))/sum(xtab)


##########QDA#################
train.qda = qda(train.data,train.label)


valid.qda = predict(train.qda,valid.data)

xtab = table(valid.label,valid.qda$class)
xtab

1 - sum(diag(xtab))/sum(xtab)


#########Trees#############

library(rpart)
train.tree = cbind(train.label,train.data)
valid.tree = cbind(valid.label,valid.data)


train.rp = rpart(train.label~., data = train.tree, method = "class")

train.rp
plot(train.rp,uniform = T, margin = 0.1)
text(train.rp)
plotcp(train.rp)
printcp(train.rp)


valid.rp = predict(train.rp,valid.tree, type = "class")
xtab = table(valid.label,valid.rp)
xtab
1 - sum(diag(xtab))/sum(xtab)

