function [accuracy] = check_accuracy(stand_data,hidden_weight,output_weight)

no_samples=size(stand_data,1);
count=0;
for i = 1:no_samples
        x_i =stand_data(i,1:3) ;
        gx_h = x_i * hidden_weight;
        O_h = sigmf(gx_h,[1 0]);
        gx_k =  O_h * output_weight; 
        O_k = sigmf(gx_k,[1 0]);
         if(O_k(:,1) > O_k(:,2))
           class = 0;
         else
           class = 1;
         end
          if(class == stand_data(i,4))
             count = count+1;
         end
end
accuracy=count/no_samples * 100 ;
return
end