clear;
%set(gcf,'unit','centimeters','position',[10 5 35 30]);
set(gcf,'unit','centimeters','position',[10 5 12 12]);
x = [1	2	3	4	5];
y1 = [1.76	2.25	4.9	6.3	16];
y2=[9.4	9.168	5.62	7.387	16.656];
y3 = [149.8	25	9.576	16.879	37.131];
y4 = [890.6 10000 20.1 18.3 45.2];
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
set(gca,'YTickLabel',{'1' '10' '10^2'  '10^3' 'Inf'})
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
 print(fig,'/Users/liboxuan/Dropbox/应用/Overleaf/MClique Algo/figures/varymotifsize-dblp.pdf','-dpdf');

% h=legend(gca,'META  ','META-ES-DA  ','META-ES-ANE  ','META-ES  ','META-Basic  ','location','best');
% set(h,'Fontsize',20,'fontweight','bold', 'Orientation','horizontal')
% set(h,'Box','off');
