files=(../core/plugin.xml ../feature-linux/feature.xml ../feature-osx/feature.xml ../feature-win32/feature.xml ../javahl-linux/META-INF/MANIFEST.MF ../javahl-linux/fragment.xml ../javahl-osx/META-INF/MANIFEST.MF ../javahl-osx/fragment.xml ../javahl-win32/META-INF/MANIFEST.MF ../javahl-win32/fragment.xml ../ui/plugin.xml ../update-site/.sitebuild/sitebuild.xml ../update-site/site.xml)
usage="./bump.sh <version>"

if [ ! $# -eq 2 ];then
    echo $usage
    exit 1
fi

version1=$1
version2=$2

for f in ${files[@]};do
    echo "bumping $f"
    mv $f $f.bak
    cat $f.bak | sed "s/$version1/$version2/g" > $f
    rm $f.bak
done
