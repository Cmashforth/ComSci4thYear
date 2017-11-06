rm(list=ls())

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
fit.model = lm(train.label ~ .,data = train.data)
pred.class = predict(fit.model,valid.data)
ifelse(pred.class >= 0.5, pred.class <- 1, pred.class <- 0)
corr.class.rate <- sum(test.label == pred.class)/ nrow(test.data)
