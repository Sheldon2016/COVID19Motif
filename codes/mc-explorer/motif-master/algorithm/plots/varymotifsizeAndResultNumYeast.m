clear;
%set(gcf,'unit','centimeters','position',[10 5 35 30]);
set(gcf,'unit','centimeters','position',[10 5 12 12]);
x = [1	2	3	4	5];
y1 = [0.044	0.128	0.341	0.327	0.531];
y2 = [0.2	0.625	1.976	0.883	1.581];
y3 = [0.209	1.342	3.87	1.653	2.325];
y4 = [0.21	1.786	5.894	1.942	2.538];
%y5=[1000 1000 1000 1000 1000];

linesize=12;
linewidth=3;
h(1) = plot(x,y1,'co-','linewidth',linewidth,'MarkerSize',linesize);
%errorbar(x,y1,y1_error,'r*-','linewidth',linewidth,'MarkerSize',linesize);
hold on;
h(2) = plot(x,y2,'r*-','linewidth',linewidth,'MarkerSize',linesize);
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
%h(5) = plot(x,y5,'g^-','linewidth',linewidth,'MarkerSize',linesize);
%errorbar(x,y5,y5_error,'msquare-','linewidth',linewidth,'MarkerSize',linesize);
%hold on;

xlabel('motif size');ylabel('Time(sec)');
axis([1 5 0.01 10]);
set(gca, 'YScale', 'log') 

%set(gca,'XTick',[1:1:5])
%set(gca,'YTick',logspace(1,4,4))
set(gca,'YTickLabel',{'10^{-2}' '10^{-1}' '1' '10'})
set(gca,'YTick',[0.01 0.1 1 10])

set(gca,'XTick',[1:1:5])
set(gca,'XTickLabel',{'3' '4' '5' '6' '7'})
set(gca,'fontsize',20,'fontweight','bold') 
grid off;
set(gca,'looseInset',[0 0 0 0]);

fig = gcf;
fig.PaperPositionMode = 'auto'
fig_pos = fig.PaperPosition;
fig.PaperSize = [fig_pos(3) fig_pos(4)];
print(fig,'C:\Users\jhu\Dropbox\Recent works\collaboration on motif\VLDB-submission\figures\varyResultNum-yeast.pdf','-dpdf');

%saveas(gca,'C:\Users\jhu\Dropbox\shared\minimal\figures\varySize-epinions.pdf') 


% h=legend(gca,'META','META-Iso','META-D','META-Basic','location','best');
% set(h,'Fontsize',16,'fontweight','bold', 'Orientation','horizontal')
% set(h,'Box','off');
