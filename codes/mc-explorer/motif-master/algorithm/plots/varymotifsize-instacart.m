clear;
%set(gcf,'unit','centimeters','position',[10 5 35 30]);
set(gcf,'unit','centimeters','position',[10 5 12 12]);
x = [1	2	3	4	5];
y1 = [3.387	2.51	13.67	21.6	24.5];
y2 = [5.245	6.139	28.4	40.8	62.9];
y3 = [135	97.9	677.8	541 10000];
y4 = [175 229 1369 10000 10000];
y5=[10000 10000 10000 10000 10000];

linesize=12;
linewidth=3;
h(1) = plot(x,y1,'r*-','linewidth',linewidth,'MarkerSize',linesize);
%errorbar(x,y1,y1_error,'r*-','linewidth',linewidth,'MarkerSize',linesize);
hold on;
h(2) = plot(x,y2,'bo-','linewidth',linewidth,'MarkerSize',linesize);
%errorbar(x,y2,y2_error,'g+-','linewidth',linewidth,'MarkerSize',linesize);
hold on;
%h(3) = plot(x,y3,'bo-','linewidth',linewidth,'MarkerSize',linesize);
%errorbar(x,y3,y3_error,'bo-','linewidth',linewidth,'MarkerSize',linesize);
hold on;
h(3) = plot(x,y3,'m+-','linewidth',linewidth,'MarkerSize',linesize);
%errorbar(x,y4,y4_error,'c^-','linewidth',linewidth,'MarkerSize',linesize);
hold on;
h(4) = plot(x,y4,'ksquare-','linewidth',linewidth,'MarkerSize',linesize);
%errorbar(x,y5,y5_error,'msquare-','linewidth',linewidth,'MarkerSize',linesize);
hold on;
h(5) = plot(x,y5,'g^-','linewidth',linewidth,'MarkerSize',linesize);
%errorbar(x,y5,y5_error,'msquare-','linewidth',linewidth,'MarkerSize',linesize);
hold on;

xlabel('motif size');ylabel('Time(sec)');
axis([1 5 1 10000]);
set(gca, 'YScale', 'log') 

%set(gca,'XTick',[1:1:5])
%set(gca,'YTick',logspace(1,4,4))
set(gca,'YTickLabel',{'1' '10' '10^2' '10^3' 'Inf'})
set(gca,'YTick',[1 10 100 1000 10000])

set(gca,'XTick',[1:1:5])
set(gca,'XTickLabel',{'3' '4' '5' '6' '7'})
set(gca,'fontsize',20,'fontweight','bold') 
grid off;
set(gca,'looseInset',[0 0 0 0]);

fig = gcf;
fig.PaperPositionMode = 'auto'
fig_pos = fig.PaperPosition;
fig.PaperSize = [fig_pos(3) fig_pos(4)];
print(fig,'C:\Users\jhu\Dropbox\Recent works\collaboration on motif\ICDE-submission\figures\varymotifsize-instacart.pdf','-dpdf');

% h=legend(gca,'META','META-Iso','META-Basic','location','best');
% set(h,'Fontsize',16,'fontweight','bold', 'Orientation','horizontal')
% set(h,'Box','off');
