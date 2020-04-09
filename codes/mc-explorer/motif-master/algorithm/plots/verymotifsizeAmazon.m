clear;
%set(gcf,'unit','centimeters','position',[10 5 35 30]);
set(gcf,'unit','centimeters','position',[10 5 12 12]);
x = [1	2	3	4	5];
y1 = [0.4	0.58	1.07	0.63	0.511];
y2 = [0.49	0.699	1.327	0.754	0.585];
y3 = [0.829	2.573	14.91	7.35	3.498];
y4 = [0.952	2.973	20.3	17.515	4.001];
y5=[1000 1000 1000 1000 1000];

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
axis([1 5 0.1 1000]);
set(gca, 'YScale', 'log') 

%set(gca,'XTick',[1:1:5])
%set(gca,'YTick',logspace(1,4,4))
set(gca,'YTickLabel',{'10^{-1}' '1' '10' '10^2'  'Inf'})
set(gca,'YTick',[0.1 1 10 100 1000])

set(gca,'XTick',[1:1:5])
set(gca,'XTickLabel',{'3' '4' '5' '6' '7'})
set(gca,'fontsize',20,'fontweight','bold') 
grid off;
set(gca,'looseInset',[0 0 0 0]);

fig = gcf;
fig.PaperPositionMode = 'auto'
fig_pos = fig.PaperPosition;
fig.PaperSize = [fig_pos(3) fig_pos(4)];
print(fig,'C:\Users\jhu\Dropbox\Recent works\collaboration on motif\VLDB-submission\figures\varymotifsize-amazon.pdf','-dpdf');

%saveas(gca,'C:\Users\jhu\Dropbox\shared\minimal\figures\varySize-epinions.pdf') 


% h=legend(gca,'META','META-Iso','META-D','META-Basic','location','best');
% set(h,'Fontsize',16,'fontweight','bold', 'Orientation','horizontal')
% set(h,'Box','off');
