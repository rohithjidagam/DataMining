
A=csvread('C:/Users/rohith/Downloads/ML/DataSet.csv');

min_x = min(A(:,1));
min_y = min(A(:,2));

if min_x < 0
  trans_x =  A(:,1) - min_x + 10;
  else
  trans_x = A(:,1);
end

if min_y < 0
  trans_y =  A(:,2) - min_y + 10;
  else
  trans_y = A(:,2);
end

class = A(:,3);
x0 = ones(400,1);

trans = horzcat(trans_x,trans_y, x0,class);

min_trans_x = min(trans(:,1));
max_trans_x = max(trans(:,1));
min_trans_y = min(trans(:,2));
max_trans_y = max(trans(:,2));
mean_trans_x =mean(trans(:,1));
mean_trans_y =mean(trans(:,2));

new_X= (trans(:,1)-mean_trans_x)/(max_trans_x-min_trans_x);
new_Y= (trans(:,2)-mean_trans_y)/(max_trans_y-min_trans_y);

stand = horzcat(new_X,new_Y,x0,class);
%csvwrite('final.csv',horzcat(trans(:,1),trans(:,2),class));
a_h=-1/sqrt(3);
b_h=1/sqrt(3);
hidden_weight = (b_h-a_h)*rand(3,17)+a_h;
hidden_weight = hidden_weight/10;
a_o = -1/sqrt(17);
b_o = 1/sqrt(17);
output_weight = (b_o - a_o)*rand(17,2)+a_o;
output_weight = output_weight/10;
initial_hidden_weight = hidden_weight;
initial_output_weight = output_weight;

n=0.5;
no_samples=size(stand,1);
k=1;
sprintf('Initial Weights::')
display(initial_hidden_weight)
display(initial_output_weight)

for epoc = 1:1000
  for i = 1:no_samples
    x_i = stand(i,1:3);
    gx_h = x_i * hidden_weight;
    O_h = sigmf(gx_h,[1 0]);
    gx_k =  O_h * output_weight; 
    O_k = sigmf(gx_k,[1 0]);
    if (stand(i,4) == 0)
        t = [1 0];
    else
        t = [0 1];
    end
    delta_k = O_k .* (1-O_k) .* (t-O_k);
    weighted_sum = bsxfun(@times,delta_k',output_weight');
    weighted_sum=weighted_sum';
    agg_sum = sum(weighted_sum,2);
    delta_h = O_h .* (1-O_h) .* agg_sum' ;
 
    rep_delta_h = kron(delta_h,ones(3,1));
    delta_h_w = bsxfun(@times,rep_delta_h,x_i');
    delta_h_w_f = delta_h_w * n ;
    hidden_weight=hidden_weight+delta_h_w_f;
    
    rep_delta_k = kron(delta_k,ones(17,1));
    delta_o_w = bsxfun(@times,rep_delta_k,O_h');
    delta_o_w_f = delta_o_w * n;
    output_weight=output_weight+delta_o_w_f;
    
  end
  %accuracy calculation
  if(rem(epoc,10) == 0)
      accuracy = check_accuracy(stand,hidden_weight,output_weight);
      if(epoc == 10 || epoc == 100 || epoc == 1000)
         display(accuracy)
      end
      accuracy_array(k) = accuracy;
      epoc_array(k)=epoc;
      k=k+1;
  end
end
sprintf('Updated Weights::')
display(hidden_weight)
display(output_weight)

plot(epoc_array,accuracy_array);
title('epoc vs accuracy');
xlabel('epoc');
ylabel('accuracy');
