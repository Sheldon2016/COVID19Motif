clear;
set(gcf,'unit','centimeters','position',[10 5 12 12]);
x = [1	2	3	4	5];
y1 = [0.006215 0.005728 0.008069 0.006127 0.00685];
y2 = [0.958774 0.686078 26.2485 1.25308 11.4335];

linesize=12;
linewidth=3;
h(1) = plot(x,y1,'r*-','linewidth',linewidth,'MarkerSize',linesize);
hold on;
h(2) = plot(x,y2,'bo-','linewidth',linewidth,'MarkerSize',linesize);
hold on;

xlabel('motif size');ylabel('Time(sec)');
axis([1 5 0.001 100]);
set(gca, 'YScale', 'log') 

set(gca,'YTickLabel',{'10^{-3}' '10^{-2}' '10^{-1}' '1' '10' '100'})
set(gca,'YTick',[0.001 0.01 0.1 1 10 100])

set(gca,'XTick',[1:1:5])
set(gca,'XTickLabel',{'3' '4' '5' '6' '7'})
set(gca,'fontsize',20,'fontweight','bold') 
grid off;
set(gca,'looseInset',[0 0 0 0]);

fig = gcf;
fig.PaperPositionMode = 'auto'
fig_pos = fig.PaperPosition;
fig.PaperSize = [fig_pos(3) fig_pos(4)];
print(fig,'/Users/liboxuan/Dropbox/应用/Overleaf/MClique Algo/figures/dynamic_graph/removeNode/varymotifsize-yeast.pdf','-dpdf');
