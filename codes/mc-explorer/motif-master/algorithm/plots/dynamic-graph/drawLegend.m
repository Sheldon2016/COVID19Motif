clear;
set(gcf,'unit','centimeters','position',[10 5 35 2]);

linesize=12;
linewidth=3;
h(1) = plot(nan, nan, 'r*-','linewidth',linewidth,'MarkerSize',linesize);
hold on;
h(2) = plot(nan, nan, 'bo-','linewidth',linewidth,'MarkerSize',linesize);
hold on;

% set(h, 'visible', 'off');

set(gca, 'visible', 'off');

fig = gcf;
fig.PaperPositionMode = 'auto'
fig_pos = fig.PaperPosition;
fig.PaperSize = [fig_pos(3) fig_pos(4)];

h=legend(gca,'META-Dyn','Baseline','location','north');
set(h,'Fontsize',20, 'fontweight','bold', 'Orientation','horizontal')
set(h,'Box','off');
print(fig,'/Users/liboxuan/Dropbox/应用/Overleaf/MClique Algo/figures/dynamic_graph/legend.pdf','-dpdf');
